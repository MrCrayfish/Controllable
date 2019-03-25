package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.GuiButtonController;
import com.mrcrayfish.controllable.client.gui.GuiControllerSelection;
import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    private ControllerManager manager;

    public GuiEvents(ControllerManager manager)
    {
        this.manager = manager;
    }

    @SubscribeEvent
    public void onOpenGui(GuiScreenEvent.InitGuiEvent event)
    {
        /* Resets the controller button states */
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            controller.resetButtonStates();
        }

        if(event.getGui() instanceof GuiMainMenu)
        {
            int y = (event.getGui().height / 4 + 48) + 24 * 2;
            event.getButtonList().add(new GuiButtonController(6969, (event.getGui().width / 2) - 124, y));
        }
        else if(event.getGui() instanceof GuiIngameMenu)
        {
            int y = (event.getGui().height / 4 + 32) + 24 * 2;
            event.getButtonList().add(new GuiButtonController(6969, (event.getGui().width / 2) - 124, y));
        }
    }

    @SubscribeEvent
    public void onAction(GuiScreenEvent.ActionPerformedEvent event)
    {
        if(event.getGui() instanceof GuiMainMenu)
        {
            if(event.getButton().id == 6969)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiControllerSelection(manager, true));
            }
        }
        else if(event.getGui() instanceof GuiIngameMenu)
        {
            if(event.getButton().id == 6969)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiControllerSelection(manager, false));
            }
        }
    }
}
