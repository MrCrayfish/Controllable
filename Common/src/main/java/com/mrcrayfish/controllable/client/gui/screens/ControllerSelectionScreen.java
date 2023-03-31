package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.ControllerManager;
import com.mrcrayfish.controllable.client.gui.IControllerList;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ControllerSelectionScreen extends Screen
{
    private int controllerCount;
    private final ControllerManager manager;
    private IControllerList<?> listControllers;
    private final Screen previousScreen;
    private Button btnSettings;
    private Button btnRemap;
    private Button btnLayout;
    private Button btnBack;

    public ControllerSelectionScreen(ControllerManager manager, Screen previousScreen)
    {
        super(Component.translatable("controllable.gui.title.select_controller"));
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getControllerCount();
    }

    @Override
    protected void init()
    {
        this.listControllers = ClientServices.CLIENT.createControllerList(this.manager, this.minecraft, this.width, this.height);
        this.addWidget(this.listControllers);
        this.btnSettings = this.addRenderableWidget(ScreenUtil.button(this.width / 2 - 154, this.height - 32, 72, 20, Component.translatable("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addRenderableWidget(ScreenUtil.button(this.width / 2 - 76, this.height - 32, 72, 20, Component.translatable("controllable.gui.binding"), this::handleConfigure));
        this.btnLayout = this.addRenderableWidget(ScreenUtil.button(this.width / 2 + 4, this.height - 32, 72, 20, Component.translatable("controllable.gui.layout"), this::handleLayout));
        this.btnBack = this.addRenderableWidget(ScreenUtil.button(this.width / 2 + 82, this.height - 32, 72, 20, Component.translatable("controllable.gui.back"), this::handleCancel));
        this.btnRemap.active = this.listControllers.getSelected() != null;
    }

    @Override
    public void tick()
    {
        if(this.controllerCount != this.manager.getControllerCount())
        {
            this.controllerCount = this.manager.getControllerCount();
            this.listControllers.reload();
        }
        this.listControllers.updateSelected();
        this.btnRemap.active = this.listControllers.getSelected() != null;
        this.btnLayout.active = this.listControllers.getSelected() != null;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        this.listControllers.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void handleSettings(Button button)
    {
        Objects.requireNonNull(this.minecraft).setScreen(new SettingsScreen(this));
    }

    private void handleConfigure(Button button)
    {
        Objects.requireNonNull(this.minecraft).setScreen(new ButtonBindingScreen(this));
    }

    private void handleLayout(Button button)
    {
        Objects.requireNonNull(this.minecraft).setScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(Button button)
    {
        Objects.requireNonNull(this.minecraft).setScreen(this.previousScreen);
    }
}
