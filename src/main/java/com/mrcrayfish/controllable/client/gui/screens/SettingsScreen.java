package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
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
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderDirtBackground(poseStack);
        boolean waitingForInput = this.isWaitingForButtonInput();
        super.render(poseStack, !waitingForInput ? mouseX : -1, !waitingForInput ? mouseY : -1, partialTick);
        if(waitingForInput)
        {
            RenderSystem.disableDepthTest();
            fillGradient(poseStack, 0, 0, this.width, this.height, 0xE0101010, 0xF0101010);
            ScreenUtil.drawRoundedBox(poseStack, (int) (this.width * 0.125), this.height / 4, (int) (this.width * 0.75), this.height / 2, 0x99000000);
            Component pressButtonLabel = Component.translatable("controllable.gui.waiting_for_input").withStyle(ChatFormatting.YELLOW);
            drawCenteredString(poseStack, this.font, pressButtonLabel, this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);
            Component time = Component.literal(Integer.toString((int) Math.ceil(this.remainingTime / 20.0)));
            Component inputCancelLabel = Component.translatable("controllable.gui.input_cancel", time);
            drawCenteredString(poseStack, this.font, inputCancelLabel, this.width / 2, this.height / 2 + 3, 0xFFFFFFFF);
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
        private static final Component TITLE = Component.empty().append(ClientHelper.getIcon(Icons.CONTROLLER)).append(" ").append(Component.translatable("controllable.settings.tab.controller.title"));

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
        private static final Component TITLE = Component.empty().append(ClientHelper.getIcon(Icons.SETTINGS)).append(" ").append(Component.translatable("controllable.settings.tab.settings.title"));

        public SettingsTab()
        {
            super(TITLE);
            Minecraft mc = Objects.requireNonNull(SettingsScreen.this.minecraft);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            TabSelectionList<TabSelectionList.BaseItem> optionsList = new TabSelectionList<>(SettingsScreen.this.minecraft, 24);

            // Restore button
            Component restoreDefaults = Component.empty().append(ClientHelper.getIcon(Icons.RESET)).append(" ").append(Component.translatable("controllable.gui.restoreDefaults"));
            optionsList.addEntry(new TabSelectionList.ButtonItem(restoreDefaults, btn -> {
                mc.setScreen(new ConfirmationScreen(SettingsScreen.this, Component.translatable("controllable.gui.restoreDefaults"), result -> {
                    if(result) {
                        Config.CLIENT.options.restoreDefaults();
                    }
                    return true;
                }));
            }));

            // Gameplay options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.gameplay").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>(Component.translatable("controllable.options.sneakMode"), Component.translatable("controllable.options.sneakMode.desc"), () -> {
                return mc.options.toggleCrouch().get() ? SneakMode.TOGGLE : SneakMode.HOLD;
            }, sneakMode -> {
                mc.options.toggleCrouch().set(sneakMode == SneakMode.TOGGLE);
                mc.options.save();
            }));
            optionsList.addEntry(new TabOptionEnumItem<>(Component.translatable("controllable.options.sprintMode"), Component.translatable("controllable.options.sprintMode.desc"), () -> {
                return mc.options.toggleSprint().get() ? SprintMode.TOGGLE : SprintMode.ONCE;
            }, sprintMode -> {
                mc.options.toggleSprint().set(sprintMode == SprintMode.TOGGLE);
                mc.options.save();
            }));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.quickCraft", Config.CLIENT.options.quickCraft));
            optionsList.addEntry(new TabOptionEnumItem<>("controllable.options.radialThumbstick", Config.CLIENT.options.radialThumbstick));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.spyglassSensitivity", Config.CLIENT.options.spyglassSensitivity, 0.05, 0.0, 1.0));

            // Camera options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.camera").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.rotationSpeed", Config.CLIENT.options.rotationSpeed, 1.0, 0.0, 100.0));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.pitchSensitivity", Config.CLIENT.options.pitchSensitivity, 0.01, 0.0, 1.0));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.yawSensitivity", Config.CLIENT.options.yawSensitivity, 0.01, 0.0, 1.0));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.invertLook", Config.CLIENT.options.invertLook));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.invertRotation", Config.CLIENT.options.invertRotation));

            // Display options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.display").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionEnumItem<>("controllable.options.controllerIcons", Config.CLIENT.options.controllerIcons));
            optionsList.addEntry(new TabOptionEnumItem<>("controllable.options.showActions", Config.CLIENT.options.showButtonHints));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.hintBackground", Config.CLIENT.options.drawHintBackground));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.consoleHotbar", Config.CLIENT.options.consoleHotbar));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.renderMiniPlayer", Config.CLIENT.options.renderMiniPlayer));

            // Controller options
            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.controller").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.autoSelect", Config.CLIENT.options.autoSelect));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.virtualMouse", Config.CLIENT.options.virtualCursor));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.deadZone", Config.CLIENT.options.thumbstickDeadZone, 0.01, 0.0, 1.0));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.triggerDeadZone", Config.CLIENT.options.triggerDeadZone, 0.01, 0.0, 1.0));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.mouseSpeed", Config.CLIENT.options.cursorSpeed, 1.0, 0.0, 50.0));
            optionsList.addEntry(new TabOptionEnumItem<>("controllable.options.cursorThumbstick", Config.CLIENT.options.cursorThumbstick));
            optionsList.addEntry(new TabOptionEnumItem<>("controllable.options.cursorType", Config.CLIENT.options.cursorType));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.listScrollSpeed", Config.CLIENT.options.listScrollSpeed, 1.0, 1.0, 30.0));
            optionsList.addEntry(new TabOptionSliderItem("controllable.options.hoverModifier", Config.CLIENT.options.hoverModifier, 0.05, 0.0, 1.0));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.forceFeedback", Config.CLIENT.options.rumble));

            optionsList.addEntry(new TabOptionTitleItem(Component.translatable("controllable.gui.title.other").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.uiSounds", Config.CLIENT.options.uiSounds));
            optionsList.addEntry(new TabOptionToggleItem("controllable.options.fpsPollingFix", Config.CLIENT.options.fpsPollingFix));

            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, optionsList));
        }
    }

    public class BindingsTab extends GridLayoutTab
    {
        private static final Component TITLE = Component.empty().append(ClientHelper.getIcon(Icons.BINDINGS)).append(" ").append(Component.translatable("controllable.settings.tab.bindings.title"));

        public BindingsTab()
        {
            super(TITLE);
            GridLayout.RowHelper rootHelper = this.layout.rowSpacing(8).createRowHelper(1);
            rootHelper.addChild(new TabListWidget(() -> SettingsScreen.this.tabArea, new ButtonBindingList(SettingsScreen.this, SettingsScreen.this.minecraft, 22)));
        }
    }
}
