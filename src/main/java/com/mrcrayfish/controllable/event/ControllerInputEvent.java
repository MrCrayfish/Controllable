package com.mrcrayfish.controllable.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Author: MrCrayfish
 */
@Cancelable
public class ControllerInputEvent extends Event
{
    private int button;
    private boolean state;

    public ControllerInputEvent(int button, boolean state)
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
