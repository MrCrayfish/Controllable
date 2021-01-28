package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.ControllerManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

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
        super(new TranslationTextComponent("controllable.gui.title.select_controller"));
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getControllerCount();
    }

    @Override
    protected void init()
    {
        this.listControllers = new ControllerList(this.manager, this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.listControllers);
        this.btnSettings = this.addButton(new Button(this.width / 2 - 154, this.height - 32, 72, 20, new TranslationTextComponent("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addButton(new Button(this.width / 2 - 76, this.height - 32, 72, 20, new TranslationTextComponent("controllable.gui.binding"), this::handleConfigure));
        this.btnLayout = this.addButton(new Button(this.width / 2 + 4, this.height - 32, 72, 20, new TranslationTextComponent("controllable.gui.layout"), this::handleLayout));
        this.btnBack = this.addButton(new Button(this.width / 2 + 82, this.height - 32, 72, 20, new TranslationTextComponent("controllable.gui.back"), this::handleCancel));
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.listControllers.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void handleSettings(Button button)
    {
        this.minecraft.displayGuiScreen(new SettingsScreen(this));
    }

    private void handleConfigure(Button button)
    {
        this.minecraft.displayGuiScreen(new ButtonBindingScreen(this));
    }

    private void handleLayout(Button button)
    {
        this.minecraft.displayGuiScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(Button button)
    {
        this.minecraft.displayGuiScreen(this.previousScreen);
    }
}
