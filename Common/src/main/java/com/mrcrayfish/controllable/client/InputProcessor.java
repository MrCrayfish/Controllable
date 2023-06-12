package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Author: MrCrayfish
 */
public class InputProcessor
{
    private static InputProcessor instance;

    public static InputProcessor instance()
    {
        if(instance == null)
        {
            instance = new InputProcessor();
        }
        return instance;
    }

    private final Queue<ButtonStates> inputQueue = new ArrayDeque<>();
    private final ControllerInput input;
    private final ControllerManager manager;

    private InputProcessor()
    {
        this.input = new ControllerInput();
        this.manager = Controllable.getManager();
        TickEvents.START_RENDER.register((partialTick) -> this.pollControllerInput(false));
        TickEvents.END_RENDER.register((partialTick) -> this.pollControllerInput(false));
        TickEvents.START_CLIENT.register(() -> this.pollControllerInput(true));
        TickEvents.END_CLIENT.register(() -> this.pollControllerInput(false));
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
        if(this.manager == null)
            return;

        this.manager.tick();

        Controller currentController = this.manager.getActiveController();
        if(currentController == null)
            return;

        this.inputQueue.offer(currentController.createButtonsStates());
    }

    private void processButtonStates()
    {
        ButtonBinding.tick();
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

        Controller controller = this.manager.getActiveController();
        if(controller == null)
            return;

        //No binding so don't perform any action
        if(index == -1)
            return;

        ButtonStates states = controller.getButtonsStates();

        if(state)
        {
            if(!states.getState(index))
            {
                states.setState(index, true);
                if(screen instanceof SettingsScreen settings && settings.isWaitingForButtonInput() && settings.processButton(index))
                {
                    return;
                }
                this.input.handleButtonInput(controller, index, true, false);
            }
        }
        else if(states.getState(index))
        {
            states.setState(index, false);
            this.input.handleButtonInput(controller, index, false, false);
        }
    }

    /**
     * Allows a controller to be polled while the main thread is waiting due to FPS limit. This
     * overrides the wait behaviour of Minecraft and is off by default. Do not call this method, it
     * is internal only.
     */
    public static void queueInputsWait()
    {
        Minecraft mc = Minecraft.getInstance();
        int fps = mc.level != null || mc.screen == null && mc.getOverlay() == null ? mc.getWindow().getFramerateLimit() : 60;
        int captureCount = 4; // The amount of times to capture controller input while waiting
        for(int i = 0; i < captureCount; i++)
        {
            RenderSystem.limitDisplayFPS(fps * captureCount);
            InputProcessor.instance().gatherAndQueueControllerInput();
        }
    }

    public ControllerInput getInput()
    {
        return this.input;
    }
}
