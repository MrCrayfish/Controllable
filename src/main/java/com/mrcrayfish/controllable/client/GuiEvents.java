package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.GuiButtonController;
import com.mrcrayfish.controllable.client.gui.GuiControllerSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    @SubscribeEvent
    public void onOpenGui(GuiScreenEvent.InitGuiEvent event)
    {
        if(event.getGui() instanceof GuiMainMenu)
        {
            int y = (event.getGui().height / 4 + 48) + 24 * 2;
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
                Minecraft.getMinecraft().displayGuiScreen(new GuiControllerSelection());
            }
        }
    }
}
