package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Author: MrCrayfish
 */
public class InputProcessor
{
    private static InputProcessor instance;

    private final Queue<ButtonStates> inputQueue = new ArrayDeque<>();
    private boolean initialized;

    @ApiStatus.Internal
    public InputProcessor()
    {
        Preconditions.checkState(instance == null, "Only one instance of InputProcessor is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            TickEvents.START_RENDER.register((partialTick) -> this.pollControllerInput(false));
            TickEvents.END_RENDER.register((partialTick) -> this.pollControllerInput(false));
            TickEvents.START_CLIENT.register(() -> this.pollControllerInput(true));
            TickEvents.END_CLIENT.register(() -> this.pollControllerInput(false));
            this.initialized = true;
        }
    }

    private void pollControllerInput(boolean process)
    {
        this.gatherAndQueueControllerInput();

        if(process)
        {
            this.processButtonStates();
        }
    }

    private void gatherAndQueueControllerInput()
    {
        AdaptiveControllerManager manager = Controllable.getControllerManager();
        manager.tick();

        Controller currentController = manager.getActiveController();
        if(currentController == null || !currentController.isAccessible())
            return;

        this.inputQueue.offer(currentController.captureButtonStates());
    }

    private void processButtonStates()
    {
        while(!this.inputQueue.isEmpty())
        {
            ButtonStates states = this.inputQueue.poll();
            for(int i = 0; i < Buttons.BUTTONS.length; i++)
            {
                this.processButton(Buttons.BUTTONS[i], states);
            }
        }
    }

    private void processButton(int index, ButtonStates newStates)
    {
        boolean state = newStates.getState(index);

        Screen screen = Minecraft.getInstance().screen;
        if(screen instanceof ControllerLayoutScreen)
        {
            ((ControllerLayoutScreen) screen).processButton(index, newStates);
            return;
        }

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        ButtonStates trackedStates = controller.getTrackedButtonStates();
        if(state)
        {
            if(!trackedStates.getState(index))
            {
                trackedStates.setState(index, true);
                if(screen instanceof SettingsScreen settings && settings.isWaitingForButtonInput() && settings.processButton(index))
                    return;
                Controllable.getInputHandler().handleButtonInput(controller, index, true); // Handle on down
            }
        }
        else if(trackedStates.getState(index))
        {
            trackedStates.setState(index, false);
            Controllable.getInputHandler().handleButtonInput(controller, index, false); // Handle on release
        }
    }

    /**
     * Allows a controller to be polled while the main thread is waiting due to FPS limit. This
     * overrides the wait behaviour of Minecraft and is off by default. Do not call this method, it
     * is internal only.
     */
    public void queueInputsWait()
    {
        Minecraft mc = Minecraft.getInstance();
        int fps = mc.level != null || mc.screen == null && mc.getOverlay() == null ? mc.getWindow().getFramerateLimit() : 60;
        int captureCount = 4; // The amount of times to capture controller input while waiting
        for(int i = 0; i < captureCount; i++)
        {
            RenderSystem.limitDisplayFPS(fps * captureCount);
            this.gatherAndQueueControllerInput();
        }
    }
}
