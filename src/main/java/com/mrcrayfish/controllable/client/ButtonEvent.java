package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public class ButtonEvent
{
    private int button;
    private boolean state;

    public ButtonEvent(int button, boolean state)
    {
        this.button = button;
        this.state = state;
    }

    public int getButton()
    {
        return button;
    }

    public boolean getState()
    {
        return state;
    }
}
