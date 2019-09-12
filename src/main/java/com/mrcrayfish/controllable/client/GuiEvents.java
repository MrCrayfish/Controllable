package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.GuiButtonController;
import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        ButtonBinding.resetButtonStates();

        if(event.getGui() instanceof OptionsScreen)
        {
            int y = event.getGui().height / 6 + 72 - 6;
            event.addWidget(new GuiButtonController((event.getGui().width / 2) + 5 + 150 + 4, y, button ->
                    Minecraft.getInstance().displayGuiScreen(new ControllerSelectionScreen(manager, event.getGui()))));
        }
    }
}
