package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * Use {@link ControllerEvents#GATHER_ACTIONS} instead
 */
@Deprecated
public class GatherActionsEvent extends Event
{
    private final Map<ButtonBinding, Action> actions;
    private final ActionVisibility visibility;

    public GatherActionsEvent(Map<ButtonBinding, Action> actions, ActionVisibility visibility)
    {
        this.actions = actions;
        this.visibility = visibility;
    }

    public Map<ButtonBinding, Action> getActions()
    {
        return this.actions;
    }

    public ActionVisibility getVisibility()
    {
        return this.visibility;
    }
}
