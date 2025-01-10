package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;

/**
 * Author: MrCrayfish
 */
public class ClientBootstrap
{
    public static void init()
    {
        Controllable.init();
        ControllerEvents.init();
        OverlayRenderer.init();
    }
}
