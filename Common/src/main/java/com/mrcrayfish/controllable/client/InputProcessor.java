package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.screens.ButtonBindingScreen;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Optional;
import java.util.Queue;

/**
 * Author: MrCrayfish
 */
public class InputProcessor
{
    private static InputProcessor instance;

    public static InputProcessor get()
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
    private Controller controller;

    private InputProcessor()
    {
        this.input = new ControllerInput();
        this.manager = InputProcessor.createManager();
        TickEvents.START_RENDER.register((partialTick) -> this.pollControllerInput(false));
        TickEvents.END_RENDER.register((partialTick) -> this.pollControllerInput(false));
        TickEvents.START_CLIENT.register(() -> this.pollControllerInput(false));
        TickEvents.END_CLIENT.register(() -> this.pollControllerInput(true));
    }

    private static ControllerManager createManager()
    {
        /* Loads up the controller manager and adds a listener */
        ControllerManager manager = new ControllerManager();
        manager.addControllerListener(new IControllerListener()
        {
            @Override
            public void connected(int jid)
            {
                Minecraft.getInstance().doRunTask(() ->
                {
                    Controller controller = InputProcessor.get().getController();
                    if(controller != null)
                        return;

                    if(Config.CLIENT.client.options.autoSelect.get())
                    {
                        InputProcessor.get().setController(controller = new Controller(jid));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null && controller != null)
                    {
                        mc.getToasts().addToast(new ControllerToast(true, controller.getName()));
                    }
                });
            }

            @Override
            public void disconnected(int jid)
            {
                Minecraft.getInstance().doRunTask(() ->
                {
                    Controller controller = InputProcessor.get().getController();
                    if(controller == null || controller.getJid() != jid)
                        return;

                    InputProcessor.get().setController(null);

                    if(Config.CLIENT.client.options.autoSelect.get() && manager.getControllerCount() > 0)
                    {
                        Optional<Integer> optional = manager.getControllers().keySet().stream().min(Comparator.comparing(i -> i));
                        optional.ifPresent(minJid -> InputProcessor.get().setController(new Controller(minJid)));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null)
                    {
                        Minecraft.getInstance().getToasts().addToast(new ControllerToast(false, controller.getName()));
                    }
                });
            }
        });
        return manager;
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
        // Updates the manager, which handles hot swapping
        if(this.manager != null)
        {
            this.manager.update();
        }

        // Don't process if no controller is selected
        Controller currentController = this.controller;
        if(currentController == null)
            return;

        // Updates the internal GLFW gamepad state
        if(!currentController.updateGamepadState())
            return;

        // Capture all inputs and queue
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_A));
        states.setState(Buttons.B, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_B));
        states.setState(Buttons.X, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_X));
        states.setState(Buttons.Y, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_Y));
        states.setState(Buttons.SELECT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_BACK));
        states.setState(Buttons.HOME, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE));
        states.setState(Buttons.START, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_START));
        states.setState(Buttons.LEFT_THUMB_STICK, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB));
        states.setState(Buttons.RIGHT_THUMB_STICK, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB));
        states.setState(Buttons.LEFT_BUMPER, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER));
        states.setState(Buttons.RIGHT_BUMPER, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER));
        states.setState(Buttons.LEFT_TRIGGER, currentController.getLTriggerValue() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, currentController.getRTriggerValue() >= 0.5F);
        states.setState(Buttons.DPAD_UP, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT));
        this.inputQueue.offer(states);
    }

    private boolean getButtonState(int buttonCode)
    {
        return this.controller != null && this.controller.getGamepadState().buttons(buttonCode) == GLFW.GLFW_PRESS;
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

        Controller controller = this.controller;
        if(controller == null)
            return;

        if(controller.getMapping() != null)
        {
            index = controller.getMapping().remap(index);
        }

        //No binding so don't perform any action
        if(index == -1)
            return;

        ButtonStates states = controller.getButtonsStates();

        if(state)
        {
            if(!states.getState(index))
            {
                states.setState(index, true);
                if(screen instanceof ButtonBindingScreen)
                {
                    if(((ButtonBindingScreen) screen).processButton(index))
                    {
                        return;
                    }
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
            InputProcessor.get().gatherAndQueueControllerInput();
        }
    }

    public ControllerManager getManager()
    {
        return this.manager;
    }

    public ControllerInput getInput()
    {
        return this.input;
    }

    @Nullable
    public Controller getController()
    {
        return this.controller;
    }

    public void setController(@Nullable Controller controller)
    {
        if(controller != null)
        {
            this.controller = controller;
            Mappings.updateControllerMappings(controller);
        }
        else
        {
            this.controller = null;
        }
    }
}
