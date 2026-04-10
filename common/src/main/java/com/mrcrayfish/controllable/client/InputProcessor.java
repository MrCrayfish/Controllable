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
import java.util.ArrayList;
import java.util.List;
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
            this.processButtonFrame(states);
        }
    }

    /**
     * Processes a full captured frame of button states.
     *
     * All newly-pressed buttons in the same frame are collected first, then passed together
     * to the InputHandler so it can resolve combos across the whole batch before deciding
     * which single-button bindings to fire. This avoids the ordering problem where Y (index 3)
     * would be processed before LB (index 9) and RB (index 10) in the same frame, preventing
     * LB+RB+Y from being recognised as a complete combo.
     */
    private void processButtonFrame(ButtonStates newStates)
    {
        Screen screen = Minecraft.getInstance().screen;
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        ButtonStates trackedStates = controller.getTrackedButtonStates();

        // Layout screen handles raw events itself
        if(screen instanceof ControllerLayoutScreen layoutScreen)
        {
            for(int i = 0; i < Buttons.BUTTONS.length; i++)
                layoutScreen.processButton(Buttons.BUTTONS[i], newStates);
            return;
        }

        // Collect newly-pressed and newly-released buttons for this frame
        List<Integer> newlyPressed  = new ArrayList<>();
        List<Integer> newlyReleased = new ArrayList<>();

        for(int i = 0; i < Buttons.BUTTONS.length; i++)
        {
            int index = Buttons.BUTTONS[i];
            boolean state = newStates.getState(index);
            boolean tracked = trackedStates.getState(index);
            if(state && !tracked)
                newlyPressed.add(index);
            else if(!state && tracked)
                newlyReleased.add(index);
        }

        // Update tracked state for pressed buttons BEFORE dispatching any press events,
        // so the InputHandler can see the full set of currently-held buttons when it checks
        // whether a combo is complete.
        for(int index : newlyPressed)
        {
            trackedStates.setState(index, true);
            if(screen instanceof SettingsScreen settings && settings.isWaitingForButtonInput() && settings.processButton(index))
            {
                newlyPressed.remove((Integer) index);
                break; // SettingsScreen consumes the first button it sees
            }
        }

        // Dispatch all press events together so InputHandler can batch-resolve combos
        if(!newlyPressed.isEmpty())
            Controllable.getInputHandler().handleButtonsPressed(controller, newlyPressed);

        // Dispatch release events (order doesn't matter for releases)
        for(int index : newlyReleased)
        {
            trackedStates.setState(index, false);
            Controllable.getInputHandler().handleButtonInput(controller, index, false);
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
