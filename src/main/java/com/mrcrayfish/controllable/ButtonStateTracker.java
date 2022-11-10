package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.Buttons;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ButtonStateTracker
{
    private ButtonStates lastState;
    private final ConcurrentLinkedDeque<ButtonStateChange> stateChanges;

    public ButtonStateTracker(ButtonStates initialState)
    {
        this.lastState = initialState;
        this.stateChanges = new ConcurrentLinkedDeque<>();
    }

    /**
     * The given new state will be compared against the state from the previous
     * call to this method, and any state changes will be noted.
     */
    public synchronized ButtonStateTracker update(ButtonStates newState)
    {
        for(int button : Buttons.BUTTONS)
        {
            boolean oldPressed = lastState.getState(button);
            boolean newPressed = newState.getState(button);
            if(oldPressed != newPressed)
            {
                stateChanges.add(new ButtonStateChange(button, newPressed));
            }
        }
        lastState = newState;
        return this;
    }

    public ButtonStateChange poll()
    {
        return stateChanges.poll();
    }

    public ButtonStates getLastState()
    {
        return lastState;
    }

    public static class ButtonStateChange
    {
        public final int button;
        public final boolean state;

        public ButtonStateChange(int button, boolean state)
        {
            this.button = button;
            this.state = state;
        }
    }
}
