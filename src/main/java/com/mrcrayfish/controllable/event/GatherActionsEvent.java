package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class GatherActionsEvent extends Event
{
    private Map<ButtonBinding, Action> actions;
    private ActionVisibility visibility;

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
