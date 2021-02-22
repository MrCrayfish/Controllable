package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.Buttons;

/**
 * Author: MrCrayfish
 */
public class ButtonStates
{
    private boolean[] states;

    public ButtonStates()
    {
        this.states = new boolean[Buttons.LENGTH];
    }

    public boolean getState(int index)
    {
        if(index < 0 || index >= states.length)
            return false;
        return this.states[index];
    }

    protected void setState(int index, boolean state)
    {
        if(index < 0 || index >= states.length)
            return;
        this.states[index] = state;
    }

    public int getSize()
    {
        return this.states.length;
    }
}
