package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;

/**
 * Author: MrCrayfish
 */
public class EventHelper
{
    public static boolean postMoveEvent()
    {
        return ControllerEvents.UPDATE_MOVEMENT.post().handle();
    }

    public static boolean postInputEvent(Controller controller, Value<Integer> newButton, int button, boolean state)
    {
        return ControllerEvents.INPUT.post().handle(controller, newButton, button, state);
    }

    public static boolean postButtonEvent(Controller controller)
    {
        return ControllerEvents.BUTTON.post().handle(controller);
    }

    public static boolean postUpdateCameraEvent(Value<Float> yawSpeed, Value<Float> pitchSpeed)
    {
        return ControllerEvents.UPDATE_CAMERA.post().handle(yawSpeed, pitchSpeed);
    }

    public static boolean postRenderMiniPlayer()
    {
        return ControllerEvents.RENDER_MINI_PLAYER.post().handle();
    }
}
