package com.mrcrayfish.controllable.util;

import com.mrcrayfish.controllable.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class Utils
{
    public static ResourceLocation resource(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
