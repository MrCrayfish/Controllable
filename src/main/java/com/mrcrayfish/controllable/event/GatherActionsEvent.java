package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class GatherActionsEvent extends Event
{
    private Map<ButtonBinding, Action> actions;

    public GatherActionsEvent(Map<ButtonBinding, Action> actions)
    {
        this.actions = actions;
    }

    public Map<ButtonBinding, Action> getActions()
    {
        return this.actions;
    }
}
