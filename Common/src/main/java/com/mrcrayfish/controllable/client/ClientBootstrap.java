package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.Util;

import java.util.List;
import java.util.stream.Collectors;

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
        RenderEvents.init();
    }
}
