package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.gui.components.*;
import com.mrcrayfish.controllable.client.settings.CursorStyle;
import com.mrcrayfish.controllable.client.settings.SneakMode;
import com.mrcrayfish.controllable.client.settings.SprintMode;
import com.mrcrayfish.controllable.client.gui.widget.TabListWidget;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.framework.api.config.AbstractProperty;
import com.mrcrayfish.framework.config.FrameworkConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends Screen
{
    @Nullable
    private final Screen parent;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final List<Runnable> tickers = new ArrayList<>();
    private ScreenRectangle tabArea;
    private TabNavigationBar navigationBar;
    private Button doneButton;
    private ButtonBinding selectedBinding;
    private final java.util.Set<Integer> pendingButtons = new java.util.TreeSet<>();
    private int remainingTime;
    private int initialTab = 0;

    public SettingsScreen(@Nullable Screen parent)
    {
        super(Component.translatable("controllable.settings"));
        this.parent = parent;
    }

    public SettingsScreen(@Nullable Screen parent, int initialTab)
    {
        super(Component.translatable("controllable.settings"));
        this.parent = parent;
        this.initialTab = initialTab;
    }

    @Nullable
    public Screen getParent()
    {
        return this.parent;
    }

    @Override
    protected void init()
    {
        this.tickers.clear();
        this.navigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new ControllerTab(this), new SettingsTab(), new BindingsTab()).build();
        this.addRenderableWidget(this.navigationBar);
        this.navigationBar.selectTab(this.initialTab, false);
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> this.minecraft.setScreen(this.parent)).pos((this.width - 200) / 2, this.height - 25).width(200).build());
        this.repositionElements();
    }

    @Override
    protected void repositionElements()
    {
        if(this.navigationBar != null)
        {
            this.navigationBar.setWidth(this.width);
            this.navigationBar.arrangeElements();
            ScreenRectangle navBarArea = this.navigationBar.getRectangle();
            this.tabArea = new ScreenRectangle(0, navBarArea.height() - 1, this.width, this.height - navBarArea.height() - 30);
            this.tabManager.setTabArea(this.tabArea);
        }
        if(this.doneButton != null)
        {
            this.doneButton.setX((this.width - 200) / 2);
            this.doneButton.setY(this.height - 25);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xScroll, double yScroll)
    {
        if(super.mouseScrolled(mouseX, mouseY, xScroll, yScroll)) {
            return true;
        }

        Tab currentTab = this.tabManager.getCurrentTab();
        if(currentTab == null)
            return false;

        List<AbstractWidget> widgets = new ArrayList<>();
        currentTab.visitChildren(widgets::add);
        return widgets.stream().filter(widget -> widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, xScroll, yScroll)).count() > 0;
    }

    @Override
    public void tick()
    {
        this.tickers.forEach(Runnable::run);
        if(this.isWaitingForButtonInput())
        {
            this.remainingTime--;
            if(this.remainingTime <= 0)
            {
                this.selectedBinding = null;
                this.pendingButtons.clear();
            }
            
            // Check if all pending buttons have been released
            if(!this.pendingButtons.isEmpty())
            {
                var controller = Controllable.getController();
                if(controller != null)
                {
                    var states = controller.getTrackedButtonStates();
                    boolean anyPressed = false;
                    for(int button : this.pendingButtons)
                    {
                        if(states.getState(button))
                        {
                            anyPressed = true;
                            break;
                        }
                    }
                    
                    // If no pending buttons are pressed, finalize the binding
                    if(!anyPressed)
                    {
                        this.finalizeBinding();
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        boolean waitingForInput = this.isWaitingForButtonInput();
        super.render(graphics, !waitingForInput ? mouseX : -1, !waitingForInput ? mouseY : -1, partialTick);
        if(waitingForInput)
        {
            this.renderBlurredBackground(partialTick);
            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate(0, 0, 100);
            graphics.fillGradient(0, 0, this.width, this.height, 0xE0101010, 0xF0101010);
            ScreenHelper.drawRoundedBox(graphics, (int) (this.width * 0.125), this.height / 4, (int) (this.width * 0.75), this.height / 2, 0x99000000);
            
            if(this.pendingButtons.isEmpty())
            {
                Component pressButtonLabel = Component.translatable("controllable.gui.waiting_for_input").withStyle(ChatFormatting.YELLOW);
                graphics.drawCenteredString(this.font, pressButtonLabel, this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);
            }
            else
            {
                Component pressButtonLabel = Component.translatable("controllable.gui.multi_button_input").withStyle(ChatFormatting.GREEN);
                graphics.drawCenteredString(this.font, pressButtonLabel, this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
                
                // Show the buttons that have been pressed
                StringBuilder buttonNames = new StringBuilder();
                for(int button : this.pendingButtons)
                {
                    if(buttonNames.length() > 0)
                        buttonNames.append(" + ");
                    String name = com.mrcrayfish.controllable.client.input.Buttons.getNameForButton(button);
                    if(name != null)
                    {
                        buttonNames.append(Component.translatable(name).getString());
                    }
                }
                graphics.drawCenteredString(this.font, Component.literal(buttonNames.toString()), this.width / 2, this.height / 2 - 5, 0xFFFFFFFF);
                
                Component releaseLabel = Component.translatable("controllable.gui.release_to_confirm").withStyle(ChatFormatting.GRAY);
                graphics.drawCenteredString(this.font, releaseLabel, this.width / 2, this.height / 2 + 8, 0xFFFFFFFF);
            }
            
            Component time = Component.literal(Integer.toString((int) Math.ceil(this.remainingTime / 20.0)));
            Component inputCancelLabel = Component.translatable("controllable.gui.input_cancel", time);
            graphics.drawCenteredString(this.font, inputCancelLabel, this.width / 2, this.height / 2 + 21, 0xFFFFFFFF);
            stack.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.selectedBinding != null)
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int action, int modifiers)
    {
        if(this.selectedBinding != null)
        {
            if(key == GLFW.GLFW_KEY_ESCAPE)
            {
                this.selectedBinding = null;
            }
            return true;
        }
        if(this.navigationBar.keyPressed(key))
        {
            return true;
        }
        return super.keyPressed(key, action, modifiers);
    }

    public void setSelectedBinding(ButtonBinding binding)
    {
        if(this.tabManager.getCurrentTab() instanceof BindingsTab)
        {
            this.selectedBinding = binding;
            this.pendingButtons.clear();
            this.remainingTime = 100;
        }
    }

    public boolean isWaitingForButtonInput()
    {
        if(this.selectedBinding != null && !(this.tabManager.getCurrentTab() instanceof BindingsTab))
        {
            this.selectedBinding = null;
            this.pendingButtons.clear();
        }
        return this.selectedBinding != null;
    }

    public boolean processButton(int index)
    {
        if(this.selectedBinding != null)
        {
            // Add button to pending set
            this.pendingButtons.add(index);
            this.remainingTime = 100; // Reset timer when new button is pressed
            return true;
        }
        return false;
    }
    
    private void finalizeBinding()
    {
        if(this.selectedBinding != null && !this.pendingButtons.isEmpty())
        {
            if(this.pendingButtons.size() == 1)
            {
                // Single button binding
                ButtonBinding.setButton(this.selectedBinding, this.pendingButtons.iterator().next());
            }
            else
            {
                // Multi-button binding
                ButtonBinding.setButtons(this.selectedBinding, new java.util.TreeSet<>(this.pendingButtons));
            }
            
            this.selectedBinding = null;
            this.pendingButtons.clear();
            BindingRegistry registry = Controllable.getBindingRegistry();
            registry.rebuildCache();
            registry.save();
        }
    }

    public class ControllerTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIconComponent(Icons.CONTROLLER)).append(" ").append(Component.translatable("controllable.settings.tab.controller.title"));

        public ControllerTab(SettingsScreen screen)
        {
            super(TITLE);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            ControllerList list = new ControllerList(SettingsScreen.this, SettingsScreen.this.minecraft, 24);
            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, list));
            screen.tickers.add(list::tick);
        }
    }

    public class SettingsTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIconComponent(Icons.SETTINGS)).append(" ").append(Component.translatable("controllable.settings.tab.settings.title"));

        public SettingsTab()
        {
            super(TITLE);
            Minecraft mc = Objects.requireNonNull(SettingsScreen.this.minecraft);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            FilteredTabSelectionList<TabSelectionList.BaseItem> optionsList = new FilteredTabSelectionList<>(SettingsScreen.this.minecraft, 24);

            // Restore button
            // Update mappings and restore button
            Component updateMappings = ClientHelper.join(Icons.WORLD, Component.translatable("controllable.gui.update_mappings"));
            Component restoreDefaults = ClientHelper.join(Icons.RESET, Component.translatable("controllable.gui.restore_defaults"));
            optionsList.addEntry(new ButtonBindingList.TwoWidgetItem(Button.builder(updateMappings, btn -> {
                ConfirmationScreen updateConfirmation = new ConfirmationScreen(SettingsScreen.this, Component.translatable("controllable.gui.update_mapping_message", Component.literal(AdaptiveControllerManager.MAPPINGS_URL).withStyle(ChatFormatting.YELLOW)), result -> {
                    if(result) {
                        Controllable.getControllerManager().downloadMappings(SettingsScreen.this);
                        return false;
                    }
                    return true;
                });
                updateConfirmation.setPositiveText(ClientHelper.join(Icons.DOWNLOAD, Component.translatable("controllable.gui.download")));
                updateConfirmation.setNegativeText(CommonComponents.GUI_CANCEL);
                updateConfirmation.setIcon(ConfirmationScreen.Icon.INFO);
                mc.setScreen(updateConfirmation);
            }).build(), Button.builder(restoreDefaults, btn -> {
                mc.setScreen(new ConfirmationScreen(SettingsScreen.this, Component.translatable("controllable.gui.restore_defaults"), result -> {
                    if(result){
                        FrameworkConfigManager.FrameworkConfigImpl config = FrameworkConfigManager.getInstance().getConfig(Config.CLIENT_CONFIG_ID);
                        if(config != null) {
                            config.getAllProperties().forEach(AbstractProperty::restoreDefault);
                            mc.setScreen(new SettingsScreen(SettingsScreen.this.parent, 1));
                            return false;
                        }
                    }
                    return true;
                }));
            }).build()));

            Component radialMenuLabel = ClientHelper.join(Icons.SETTINGS, Component.translatable("controllable.gui.title.radial_menu_configure"));
            optionsList.addEntry(new ButtonBindingList.OneWidgetItem(Button.builder(radialMenuLabel, button -> {
                Controllable.getRadialMenu().load();
                Minecraft.getInstance().setScreen(new RadialMenuConfigureScreen(SettingsScreen.this));
            }).build()));

            // Gameplay options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.gameplay").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>(Component.translatable("controllable.gui.sneak_mode"), Component.translatable("controllable.gui.sneak_mode.desc"), () -> {
                return mc.options.toggleCrouch().get() ? SneakMode.TOGGLE : SneakMode.HOLD;
            }, sneakMode -> {
                mc.options.toggleCrouch().set(sneakMode == SneakMode.TOGGLE);
                mc.options.save();
            }));
            optionsList.addEntry(new TabOptionEnumItem<>(Component.translatable("controllable.gui.sprint_mode"), Component.translatable("controllable.gui.sprint_mode.desc"), () -> {
                return mc.options.toggleSprint().get() ? SprintMode.TOGGLE : SprintMode.ONCE;
            }, sprintMode -> {
                mc.options.toggleSprint().set(sprintMode == SprintMode.TOGGLE);
                mc.options.save();
            }));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.quickCraft).setChangeCallback(value -> {
                if(mc.player != null && mc.level != null) {
                    // Rebuild the client recipe book
                    ClientRecipeBook book = mc.player.getRecipeBook();
                    Iterable<RecipeHolder<?>> holders = book.getCollections().stream().flatMap(c -> c.getRecipes().stream()).toList();
                    RegistryAccess access = mc.level.registryAccess();
                    book.setupCollections(holders, access);
                    book.getCollections().forEach(c -> c.updateKnownRecipes(book));
                }
            }));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.options.radialThumbstick));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.spyglassSensitivity, 0.05));

            // Camera options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.camera").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.rotationSpeed, 1.0));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.pitchSensitivity, 0.01));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.yawSensitivity, 0.01));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.invertLook));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.invertRotation));

            // Display options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.display").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.options.controllerIcons));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.options.showButtonHints));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.drawHintBackground));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.consoleHotbar));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.paperDoll));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.overlayTimeout));

            // Controller options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.controller").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.options.analogMovement));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.autoSelect));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.backgroundInput));

            TabOptionSliderItem deadzoneOption = new TabOptionSliderItem(Config.CLIENT.options.thumbstickDeadZone, 0.01);
            deadzoneOption.setVisibilityCondition(() -> !Config.CLIENT.options.advanced.advancedMode.get());
            optionsList.addEntry(deadzoneOption);

            TabOptionSliderItem leftDeadzoneOption = new TabOptionSliderItem(Config.CLIENT.options.advanced.leftThumbstickDeadZone, 0.01);
            leftDeadzoneOption.setVisibilityCondition(Config.CLIENT.options.advanced.advancedMode::get);
            optionsList.addEntry(leftDeadzoneOption);

            TabOptionSliderItem rightDeadzoneOption = new TabOptionSliderItem(Config.CLIENT.options.advanced.rightThumbstickDeadZone, 0.01);
            rightDeadzoneOption.setVisibilityCondition(Config.CLIENT.options.advanced.advancedMode::get);
            optionsList.addEntry(rightDeadzoneOption);

            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.triggerDeadZone, 0.01));

            TabOptionToggleItem disableVirtualCursor = new TabOptionToggleItem(Config.CLIENT.options.disableVirtualCursor);
            optionsList.addEntry(disableVirtualCursor);

            TabOptionSliderItem cursorSpeed = new TabOptionSliderItem(Config.CLIENT.options.cursorSpeed, 1.0);
            cursorSpeed.setDependentOption(disableVirtualCursor, true);
            optionsList.addEntry(cursorSpeed);

            TabOptionEnumItem<CursorStyle> cursorType = new TabOptionEnumItem<>(Config.CLIENT.options.cursorType);
            cursorType.setDependentOption(disableVirtualCursor, true);
            optionsList.addEntry(cursorType);

            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.listScrollSpeed, 1.0));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.options.hoverModifier, 0.05));
            if(!Minecraft.ON_OSX)
            {
                optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.rumble));
            }

            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.other").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.navigateSound));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.options.fpsPollingFix));
            TabOptionToggleItem advancedModeOption = new TabOptionToggleItem(Config.CLIENT.options.advanced.advancedMode);
            advancedModeOption.setChangeCallback(aBoolean -> optionsList.rebuildList(true));
            optionsList.addEntry(advancedModeOption);

            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, optionsList));

            optionsList.rebuildList(false);
        }
    }

    public class BindingsTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIconComponent(Icons.BINDINGS)).append(" ").append(Component.translatable("controllable.settings.tab.bindings.title"));

        public BindingsTab()
        {
            super(TITLE);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, new ButtonBindingList(SettingsScreen.this, SettingsScreen.this.minecraft, 24)));
        }
    }
}
