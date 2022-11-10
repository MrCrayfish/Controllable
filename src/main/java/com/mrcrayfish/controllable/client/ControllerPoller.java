package com.mrcrayfish.controllable.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mrcrayfish.controllable.ButtonStateTracker;
import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControllerPoller
{
    /**
     * Rate of controller polling, in hertz (i.e. per-second)
     * Common controller poll rates:
     * - Xbox One: 125Hz
     * - PS4 DualShock 4: 250Hz (USB) / 500Hz (Bluetooth)
     * https://forums.guru3d.com/threads/anybody-know-what-polling-rate-xbox-controllers-use-on-windows-10.422404/
     */
    private static final int POLL_RATE_HZ = 125;

    private final ControllerManager manager;
    private final WeakHashMap<Controller, ButtonStateTracker> stateTrackers;
    private ScheduledExecutorService executor;

    public ControllerPoller(ControllerManager manager)
    {
        this.manager = manager;
        this.stateTrackers = new WeakHashMap<>(4);
    }

    public boolean isAsyncPollingEnabled()
    {
        return ControllerProperties.isAsyncPollingEnabled();
    }

    public void startAsyncPolling()
    {
        if(!isAsyncPollingEnabled() || this.executor != null)
        {
            return;
        }

        this.executor = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("Controllable-Poller-%d").setDaemon(true).build());
        // by using daemon threads, we don't have to worry about cleanup during exit

        // TODO the poll rate should be configurable
        executor.scheduleAtFixedRate(this::poll, 0, 1000000000L / POLL_RATE_HZ, TimeUnit.NANOSECONDS);
    }

    /**
     * Poll will call the ControllerManager to update the list of controllers, and then
     * assuming a controller is connected, will update the state of that controller.
     * If async polling is enabled, this method will be called from a background thread;
     * it can be called multiple times before the client tick loop will retrieve the
     * states that were found from polling.
     */
    public void poll()
    {
        manager.update();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            controller.updateGamepadState();
            gatherAndQueueControllerInput(controller);
        }
    }

    private void gatherAndQueueControllerInput(Controller controller)
    {
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_A, controller));
        states.setState(Buttons.B, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_B, controller));
        states.setState(Buttons.X, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_X, controller));
        states.setState(Buttons.Y, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_Y, controller));
        states.setState(Buttons.SELECT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_BACK, controller));
        states.setState(Buttons.HOME, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE, controller));
        states.setState(Buttons.START, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_START, controller));
        states.setState(Buttons.LEFT_THUMB_STICK, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB, controller));
        states.setState(Buttons.RIGHT_THUMB_STICK, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, controller));
        states.setState(Buttons.LEFT_BUMPER, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, controller));
        states.setState(Buttons.RIGHT_BUMPER, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, controller));
        states.setState(Buttons.LEFT_TRIGGER, controller.getLTriggerValue() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, controller.getRTriggerValue() >= 0.5F);
        states.setState(Buttons.DPAD_UP, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP, controller));
        states.setState(Buttons.DPAD_DOWN, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN, controller));
        states.setState(Buttons.DPAD_LEFT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, controller));
        states.setState(Buttons.DPAD_RIGHT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, controller));

        stateTrackers.compute(controller, (k, tracker) ->
                tracker == null ? new ButtonStateTracker(states) : tracker.update(states));
    }

    private static boolean getButtonState(int buttonCode, Controller controller)
    {
        return controller != null && controller.getGamepadState().buttons(buttonCode) == GLFW.GLFW_PRESS;
    }

    @Nullable
    public ButtonStateTracker getStateTracker(Controller controller)
    {
        if(controller == null)
        {
            return null;
        }
        return stateTrackers.get(controller);
    }
}
