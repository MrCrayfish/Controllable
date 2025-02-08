package com.mrcrayfish.controllable.util;

import com.mrcrayfish.controllable.Constants;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

/**
 * Author: MrCrayfish
 */
public class Utils
{
    public static ResourceLocation resource(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }

    public static Path getGamePath()
    {
        return com.mrcrayfish.framework.platform.Services.CONFIG.getGamePath();
    }

    public static Path getConfigDirectory()
    {
        return com.mrcrayfish.framework.platform.Services.CONFIG.getConfigPath();
    }

    public static boolean isModLoaded(String modId)
    {
        return com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded(modId);
    }
}
