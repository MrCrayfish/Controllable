package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.framework.api.event.client.FrameworkClientTickEvents;
import net.minecraft.client.FramerateLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
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
            FrameworkClientTickEvents.START_RENDER.register((partialTick) -> this.pollControllerInput(false));
            FrameworkClientTickEvents.END_RENDER.register((partialTick) -> this.pollControllerInput(false));
            FrameworkClientTickEvents.START_CLIENT.register(() -> this.pollControllerInput(true));
            FrameworkClientTickEvents.END_CLIENT.register(() -> this.pollControllerInput(false));
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
    public void queueInputsWait(int framerateLimit)
    {
        int inputCaptureCount = 4; // The number of times to capture controller input while waiting
        for(int i = 0; i < inputCaptureCount; i++)
        {
            FramerateLimiter.limitDisplayFPS(framerateLimit * inputCaptureCount);
            this.gatherAndQueueControllerInput();
        }
    }
}
