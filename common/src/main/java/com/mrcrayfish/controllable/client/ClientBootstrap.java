package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;

/**
 * Author: MrCrayfish
 */
public class ClientBootstrap
{
    public static void init()
    {
        Controllable.init();
        RadialMenuHandler.instance();
        ControllerEvents.init();
        OverlayHandler.init();
    }
}
