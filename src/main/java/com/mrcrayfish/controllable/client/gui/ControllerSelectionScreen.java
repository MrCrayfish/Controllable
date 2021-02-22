package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.ControllerManager;
import com.mrcrayfish.controllable.client.gui.widget.PressableButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

/**
 * Author: MrCrayfish
 */
public class ControllerSelectionScreen extends GuiScreen
{
    private int controllerCount;
    private ControllerManager manager;
    private ControllerList listControllers;
    private GuiScreen previousScreen;
    private GuiButton btnSettings;
    private GuiButton btnRemap;
    private GuiButton btnLayout;
    private GuiButton btnBack;

    public ControllerSelectionScreen(ControllerManager manager, GuiScreen previousScreen)
    {
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getControllerCount();
    }

    @Override
    public void initGui()
    {
        this.listControllers = new ControllerList(this.manager, this.mc, this.width, this.height, 32, this.height - 44, 20);
        this.btnSettings = this.addButton(new PressableButton(this.width / 2 - 154, this.height - 32, 72, 20, I18n.format("controllable.gui.settings"), this::handleSettings));
        this.btnRemap = this.addButton(new PressableButton(this.width / 2 - 76, this.height - 32, 72, 20, I18n.format("controllable.gui.binding"), this::handleConfigure));
        this.btnLayout = this.addButton(new PressableButton(this.width / 2 + 4, this.height - 32, 72, 20, I18n.format("controllable.gui.layout"), this::handleLayout));
        this.btnBack = this.addButton(new PressableButton(this.width / 2 + 82, this.height - 32, 72, 20, I18n.format("controllable.gui.back"), this::handleCancel));
        this.btnRemap.enabled = this.listControllers.getSelected() != null;
    }

    @Override
    public void updateScreen()
    {
        if(this.controllerCount != this.manager.getControllerCount())
        {
            this.controllerCount = this.manager.getControllerCount();
            this.listControllers.reload();
        }
        this.listControllers.updateSelected();
        this.btnRemap.enabled = this.listControllers.getSelected() != null;
        this.btnLayout.enabled = this.listControllers.getSelected() != null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.listControllers.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("controllable.gui.title.select_controller"), this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.listControllers.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.listControllers.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.listControllers.mouseReleased(mouseX, mouseY, state);
    }

    private void handleSettings(GuiButton button)
    {
        this.mc.displayGuiScreen(new SettingsScreen(this));
    }

    private void handleConfigure(GuiButton button)
    {
        this.mc.displayGuiScreen(new ButtonBindingScreen(this));
    }

    private void handleLayout(GuiButton button)
    {
        this.mc.displayGuiScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(GuiButton button)
    {
        this.mc.displayGuiScreen(this.previousScreen);
    }
}
