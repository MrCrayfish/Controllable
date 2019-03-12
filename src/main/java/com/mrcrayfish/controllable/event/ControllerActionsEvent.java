package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Action;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ControllerActionsEvent extends Event
{
    private Map<Integer, Action> actions;

    public ControllerActionsEvent(Map<Integer, Action> actions)
    {
        this.actions = actions;
    }

    public Map<Integer, Action> getActions()
    {
        return actions;
    }
}
