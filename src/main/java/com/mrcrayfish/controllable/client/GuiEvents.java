package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    private SDL2ControllerManager manager;

    public GuiEvents(SDL2ControllerManager manager)
    {
        this.manager = manager;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onOpenGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        /* Resets the controller button states */
        ButtonBinding.resetButtonStates();

        if(event.getGui() instanceof OptionsScreen)
        {
            int y = event.getGui().height / 6 + 72 - 6;
            event.addWidget(new ControllerButton((event.getGui().width / 2) + 5 + 150 + 4, y, button ->
                    Minecraft.getInstance().displayGuiScreen(new ControllerSelectionScreen(manager, event.getGui()))));
        }
    }
}
