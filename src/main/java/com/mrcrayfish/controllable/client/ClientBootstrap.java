package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraftforge.common.MinecraftForge;

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
        MinecraftForge.EVENT_BUS.register(new ControllerEvents());
        RenderEvents.init();
    }
}
