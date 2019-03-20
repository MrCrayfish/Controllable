package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Author: MrCrayfish
 */
public class GuiControllerSelection extends GuiScreen
{
    private GuiListControllers listControllers;

    @Override
    public void initGui()
    {
        listControllers = new GuiListControllers(mc, this.width, this.height, 32, this.height - 64, 20);
        this.addButton(new GuiButton(0, this.width / 2 - 154, this.height - 52, 150, 20, "Select")); //TODO localize I18n.format("selectWorld.select")
        this.addButton(new GuiButton(1, this.width / 2 + 4, this.height - 52, 150, 20, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button.id == 0)
        {
            Controller controller = new Controller(listControllers.getSelectedController());
            Mappings.updateControllerMappings(controller);
            Controllable.setController(controller);
            this.mc.displayGuiScreen(null);
        }
        else if(button.id == 1)
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
