package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.ControllerManager;
import com.mrcrayfish.controllable.client.gui.ControllerList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class ControllerSelectionScreen extends Screen
{
    private int controllerCount;
    private ControllerManager manager;
    private ControllerList listControllers;
    private Screen previousScreen;
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
        this.listControllers = new ControllerList(this.manager, this.minecraft, this.width, this.height, 45, this.height - 44, 22);
        this.addWidget(this.listControllers);
        this.btnSettings = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 32, 72, 20, Component.translatable("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addRenderableWidget(new Button(this.width / 2 - 76, this.height - 32, 72, 20, Component.translatable("controllable.gui.binding"), this::handleConfigure));
        this.btnLayout = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 32, 72, 20, Component.translatable("controllable.gui.layout"), this::handleLayout));
        this.btnBack = this.addRenderableWidget(new Button(this.width / 2 + 82, this.height - 32, 72, 20, Component.translatable("controllable.gui.back"), this::handleCancel));
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
        this.minecraft.setScreen(new SettingsScreen(this));
    }

    private void handleConfigure(Button button)
    {
        this.minecraft.setScreen(new ButtonBindingScreen(this));
    }

    private void handleLayout(Button button)
    {
        this.minecraft.setScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(Button button)
    {
        this.minecraft.setScreen(this.previousScreen);
    }
}
