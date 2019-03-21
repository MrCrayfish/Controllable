package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Author: MrCrayfish
 */
public class GuiControllerSelection extends GuiScreen
{
    private int controllerCount;
    private ControllerManager manager;
    private GuiListControllers listControllers;

    private GuiButton btnSelect;
    private GuiButton btnConfigure;
    private GuiButton btnCancel;

    public GuiControllerSelection(ControllerManager manager)
    {
        this.manager = manager;
        this.controllerCount = manager.getNumControllers();
    }

    @Override
    public void initGui()
    {
        listControllers = new GuiListControllers(manager, mc, this.width, this.height, 32, this.height - 44, 20);
        this.addButton(btnSelect = new GuiButton(0, this.width / 2 - 154, this.height - 32, 100, 20, "Select")); //TODO localize I18n.format("selectWorld.select")
        this.addButton(btnConfigure = new GuiButton(1, this.width / 2 - 50, this.height - 32, 100, 20, "Configure"));
        this.addButton(btnCancel = new GuiButton(2, this.width / 2 + 54, this.height - 32, 100, 20, "Cancel"));
        btnConfigure.enabled = Controllable.getSelectedControllerIndex() != -1;
    }

    @Override
    public void updateScreen()
    {
        if(controllerCount != manager.getNumControllers())
        {
            controllerCount = manager.getNumControllers();
            listControllers.reload();
            listControllers.setSelectedElement(Controllable.getSelectedControllerIndex());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(!button.enabled)
            return;

        if(button.id == 0)
        {
            ControllerIndex index = manager.getControllerIndex(listControllers.getSelectedIndex());
            Controller controller = new Controller(index);
            Mappings.updateControllerMappings(controller);
            Controllable.setController(controller);
            this.mc.displayGuiScreen(null);
        }
        else if(button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiControllerLayout());
        }
        else if(button.id == 2)
        {
            this.mc.displayGuiScreen(null);
        }
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        listControllers.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "Select a Controller", this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        listControllers.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        listControllers.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        listControllers.mouseReleased(mouseX, mouseY, state);
    }
}
