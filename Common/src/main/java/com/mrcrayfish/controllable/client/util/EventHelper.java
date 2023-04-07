package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.controllable.platform.ClientServices;

/**
 * Author: MrCrayfish
 */
public class EventHelper
{
    public static boolean postMoveEvent(Controller controller)
    {
        boolean cancelled = ControllerEvents.UPDATE_MOVEMENT.post().handle();
        if(!cancelled)
        {
            cancelled = ClientServices.CLIENT.sendLegacyControllerEventMove(controller);
        }
        return cancelled;
    }

    public static boolean postInputEvent(Controller controller, Value<Integer> newButton, int button, boolean state)
    {
        boolean cancelled = ControllerEvents.INPUT.post().handle(controller, newButton, button, state);
        if(!cancelled)
        {
            cancelled = ClientServices.CLIENT.sendLegacyControllerEventButtonInput(controller, newButton, button, state);
        }
        return cancelled;
    }

    public static boolean postButtonEvent(Controller controller)
    {
        boolean cancelled = ControllerEvents.BUTTON.post().handle(controller);
        if(!cancelled)
        {
            cancelled = ClientServices.CLIENT.sendLegacyControllerEventButton(controller);
        }
        return cancelled;
    }
}
