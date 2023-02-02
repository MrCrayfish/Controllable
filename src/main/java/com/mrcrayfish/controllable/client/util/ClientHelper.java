package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public class ClientHelper
{
    public static boolean isPlayingGame()
    {
        return Minecraft.getInstance().player != null;
    }
}
