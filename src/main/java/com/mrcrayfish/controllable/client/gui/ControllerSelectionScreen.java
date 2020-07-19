package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

/**
 * Author: MrCrayfish
 */
public class ControllerSelectionScreen extends Screen
{
    private int controllerCount;
    private SDL2ControllerManager manager;
    private ControllerList listControllers;
    private Screen previousScreen;
    private Button btnSettings;
    private Button btnRemap;
    private Button btnBack;

    public ControllerSelectionScreen(SDL2ControllerManager manager, Screen previousScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.select_controller"));
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getControllers().size;
    }

    @Override
    protected void init()
    {
        this.listControllers = new ControllerList(this.manager, this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.listControllers);
        this.btnSettings = this.addButton(new Button(this.width / 2 - 154, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.remap"), this::handleConfigure));
        this.btnBack = this.addButton(new Button(this.width / 2 + 54, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.back"), this::handleCancel));
        //this.btnRemap.active = this.listControllers.getSelected() != null;
        this.btnRemap.active = false; //TODO test
    }

    @Override
    public void tick()
    {
        if(this.controllerCount != this.manager.getControllers().size)
        {
            this.controllerCount = this.manager.getControllers().size;
            this.listControllers.reload();
            //this.btnRemap.active = this.listControllers.getSelected() != null;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.listControllers.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void handleSettings(Button button)
    {
        this.minecraft.displayGuiScreen(new SettingsScreen(this));
    }

    private void handleConfigure(Button button)
    {
        this.minecraft.displayGuiScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(Button button)
    {
        this.minecraft.displayGuiScreen(this.previousScreen);
    }
}
