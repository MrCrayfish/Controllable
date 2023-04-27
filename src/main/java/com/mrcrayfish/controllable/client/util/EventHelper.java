package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import com.mrcrayfish.controllable.event.Value;
import net.minecraftforge.common.MinecraftForge;

/**
 * Author: MrCrayfish
 */
public class EventHelper
{
    public static boolean postMoveEvent(Controller controller)
    {
        return MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller));
    }

    public static boolean postInputEvent(Controller controller, Value<Integer> newButton, int button, boolean state)
    {
        var event = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(event))
            return true;
        newButton.set(event.getModifiedButton());
        return false;
    }

    public static boolean postButtonEvent(Controller controller)
    {
        return MinecraftForge.EVENT_BUS.post(new ControllerEvent.Button(controller));
    }

    public static boolean postUpdateCameraEvent(Controller controller, Value<Float> yawSpeed, Value<Float> pitchSpeed)
    {
        var event = new ControllerEvent.Turn(controller, yawSpeed.get(), pitchSpeed.get());
        if(MinecraftForge.EVENT_BUS.post(event))
            return true;
        yawSpeed.set(event.getYawSpeed());
        pitchSpeed.set(event.getPitchSpeed());
        return false;
    }

    public static boolean postRenderMiniPlayer()
    {
        return MinecraftForge.EVENT_BUS.post(new RenderPlayerPreviewEvent());
    }
}
