package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;

/**
 * Author: MrCrayfish
 */
public class ClientBootstrap
{
    public static void init()
    {
        InputProcessor.instance();
        RadialMenuHandler.instance();
        Controllable.init();
        ControllerEvents.init();
        OverlayHandler.init();
        RumbleHandler.init();
    }
}
