package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.GuiButtonController;
import com.mrcrayfish.controllable.client.gui.GuiControllerSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    private SDL2ControllerManager manager;

    public GuiEvents(SDL2ControllerManager  manager)
    {
        this.manager = manager;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onOpenGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        /* Resets the controller button states */
        ButtonBinding.resetButtonStates();

        if(event.getGui() instanceof GuiOptions)
        {
            int y = event.getGui().height / 6 + 72 - 6;
            event.getButtonList().add(new GuiButtonController(6969, (event.getGui().width / 2) + 5 + 150 + 4, y));
        }
    }

    @SubscribeEvent
    public void onAction(GuiScreenEvent.ActionPerformedEvent event)
    {
        if(event.getGui() instanceof GuiOptions)
        {
            if(event.getButton().id == 6969)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiControllerSelection(manager, true));
            }
        }
    }
}
