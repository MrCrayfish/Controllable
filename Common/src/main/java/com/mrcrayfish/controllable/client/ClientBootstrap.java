package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;

/**
 * Author: MrCrayfish
 */
public class ClientBootstrap
{
    public static void init()
    {
        ControllerManager.instance().init();
        InputProcessor.instance();
        RadialMenuHandler.instance();
        Controllable.init();
        ControllerEvents.init();
        RenderEvents.init();
    }
}
