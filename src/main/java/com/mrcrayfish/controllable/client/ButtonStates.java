package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public class ButtonStates
{
    private final boolean[] states;

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
