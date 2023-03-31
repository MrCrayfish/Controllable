package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
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
}
