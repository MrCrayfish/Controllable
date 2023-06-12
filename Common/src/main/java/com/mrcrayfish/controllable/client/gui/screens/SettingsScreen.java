package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Icons;
import com.mrcrayfish.controllable.client.SneakMode;
import com.mrcrayfish.controllable.client.SprintMode;
import com.mrcrayfish.controllable.client.gui.components.ButtonBindingList;
import com.mrcrayfish.controllable.client.gui.components.ControllerList;
import com.mrcrayfish.controllable.client.gui.components.TabOptionEnumItem;
import com.mrcrayfish.controllable.client.gui.components.TabOptionSliderItem;
import com.mrcrayfish.controllable.client.gui.components.TabOptionTitleItem;
import com.mrcrayfish.controllable.client.gui.components.TabOptionToggleItem;
import com.mrcrayfish.controllable.client.gui.components.TabSelectionList;
import com.mrcrayfish.controllable.client.gui.widget.TabListWidget;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import com.mrcrayfish.framework.api.config.AbstractProperty;
import com.mrcrayfish.framework.config.FrameworkConfigManager;
import net.minecraft.ChatFormatting;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
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
    private ScreenRectangle tabArea;
    private TabNavigationBar navigationBar;
    private Button doneButton;
    private ButtonBinding selectedBinding;
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
        this.navigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new ControllerTab(), new SettingsTab(), new BindingsTab()).build();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if(super.mouseScrolled(mouseX, mouseY, scroll)) {
            return true;
        }

        Tab currentTab = this.tabManager.getCurrentTab();
        if(currentTab == null)
            return false;

        List<AbstractWidget> widgets = new ArrayList<>();
        currentTab.visitChildren(widgets::add);
        return widgets.stream().filter(widget -> widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, scroll)).count() > 0;
    }

    @Override
    public void tick()
    {
        this.tabManager.tickCurrent();

        if(this.isWaitingForButtonInput())
        {
            this.remainingTime--;
            if(this.remainingTime <= 0)
            {
                this.selectedBinding = null;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderDirtBackground(graphics);
        boolean waitingForInput = this.isWaitingForButtonInput();
        super.render(graphics, !waitingForInput ? mouseX : -1, !waitingForInput ? mouseY : -1, partialTick);
        if(waitingForInput)
        {
            RenderSystem.disableDepthTest();
            graphics.fillGradient(0, 0, this.width, this.height, 0xE0101010, 0xF0101010);
            ScreenUtil.drawRoundedBox(graphics, (int) (this.width * 0.125), this.height / 4, (int) (this.width * 0.75), this.height / 2, 0x99000000);
            Component pressButtonLabel = Component.translatable("controllable.gui.waiting_for_input").withStyle(ChatFormatting.YELLOW);
            graphics.drawCenteredString(this.font, pressButtonLabel, this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);
            Component time = Component.literal(Integer.toString((int) Math.ceil(this.remainingTime / 20.0)));
            Component inputCancelLabel = Component.translatable("controllable.gui.input_cancel", time);
            graphics.drawCenteredString(this.font, inputCancelLabel, this.width / 2, this.height / 2 + 3, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public boolean keyPressed(int key, int action, int modifiers)
    {
        if(key == GLFW.GLFW_KEY_ESCAPE && this.selectedBinding != null)
        {
            this.selectedBinding = null;
            return true;
        }
        else if(this.navigationBar.keyPressed(key))
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
            this.remainingTime = 100;
        }
    }

    public boolean isWaitingForButtonInput()
    {
        if(this.selectedBinding != null && !(this.tabManager.getCurrentTab() instanceof BindingsTab))
        {
            this.selectedBinding = null;
        }
        return this.selectedBinding != null;
    }

    public boolean processButton(int index)
    {
        if(this.selectedBinding != null)
        {
            this.selectedBinding.setButton(index);
            this.selectedBinding = null;
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.resetBindingHash();
            registry.save();
            return true;
        }
        return false;
    }

    public class ControllerTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIconComponent(Icons.CONTROLLER)).append(" ").append(Component.translatable("controllable.settings.tab.controller.title"));

        private final ControllerList list;

        public ControllerTab()
        {
            super(TITLE);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            this.list = new ControllerList(SettingsScreen.this.minecraft, 24);
            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, this.list));
        }

        @Override
        public void tick()
        {
            this.list.tick();
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
            TabSelectionList<TabSelectionList.BaseItem> optionsList = new TabSelectionList<>(SettingsScreen.this.minecraft, 24);

            // Restore button
            // Update mappings and restore button
            Component updateMappings = ClientHelper.join(Icons.WORLD, Component.translatable("controllable.gui.update_mappings"));
            Component restoreDefaults = ClientHelper.join(Icons.RESET, Component.translatable("controllable.gui.restore_defaults"));
            optionsList.addEntry(new ButtonBindingList.TwoWidgetItem(Button.builder(updateMappings, btn -> {
                ConfirmationScreen updateConfirmation = new ConfirmationScreen(SettingsScreen.this, Component.translatable("controllable.gui.update_mapping_message", Component.literal(ControllerManager.MAPPINGS_URL).withStyle(ChatFormatting.YELLOW)), result -> {
                    if(result) {
                        Controllable.getManager().downloadMappings(SettingsScreen.this);
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
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.quickCraft));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.client.options.radialThumbstick));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.spyglassSensitivity, 0.05));

            // Camera options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.camera").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.rotationSpeed, 1.0));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.pitchSensitivity, 0.01));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.yawSensitivity, 0.01));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.invertLook));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.invertRotation));

            // Display options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.display").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.client.options.controllerIcons));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.client.options.showButtonHints));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.drawHintBackground));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.consoleHotbar));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.renderMiniPlayer));

            // Controller options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.controller").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.autoSelect));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.virtualCursor));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.thumbstickDeadZone, 0.01));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.triggerDeadZone, 0.01));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.cursorSpeed, 1.0));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.client.options.cursorThumbstick));
            optionsList.addEntry(new TabOptionEnumItem<>(Config.CLIENT.client.options.cursorType));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.listScrollSpeed, 1.0));
            optionsList.addEntry(new TabOptionSliderItem(Config.CLIENT.client.options.hoverModifier, 0.05));
            if(!Minecraft.ON_OSX)
            {
                optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.rumble));
            }

            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.other").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.uiSounds));
            optionsList.addEntry(new TabOptionToggleItem(Config.CLIENT.client.options.fpsPollingFix));

            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, optionsList));
        }
    }

    public class BindingsTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIconComponent(Icons.BINDINGS)).append(" ").append(Component.translatable("controllable.settings.tab.bindings.title"));

        public BindingsTab()
        {
            super(TITLE);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, new ButtonBindingList(SettingsScreen.this, SettingsScreen.this.minecraft, 22)));
        }
    }
}
