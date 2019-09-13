package com.mrcrayfish.controllable.client.gui;

import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
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
    private Button btnBack;

    public ControllerSelectionScreen(ControllerManager manager, Screen previousScreen)
    {
        super(new TranslationTextComponent("controllable.selectController.title"));
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getNumControllers();
    }

    @Override
    protected void init()
    {
        this.listControllers = new ControllerList(this.manager, this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.listControllers);
        this.btnSettings = this.addButton(new Button(this.width / 2 - 154, this.height - 32, 100, 20, I18n.format("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, I18n.format("controllable.gui.remap"), this::handleConfigure));
        this.btnBack = this.addButton(new Button(this.width / 2 + 54, this.height - 32, 100, 20, I18n.format("controllable.gui.back"), this::handleCancel));
        //this.btnRemap.active = this.listControllers.getSelected() != null;
        this.btnRemap.active = false;
    }

    @Override
    public void tick()
    {
        if(this.controllerCount != this.manager.getNumControllers())
        {
            this.controllerCount = this.manager.getNumControllers();
            this.listControllers.reload();
            //this.btnRemap.active = this.listControllers.getSelected() != null;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.listControllers.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, I18n.format("controllable.gui.title.select_controller"), this.width / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
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
