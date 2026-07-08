package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.util.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

/**
 * Author: MrCrayfish
 */
@JeiPlugin
public class ControllableJeiPlugin implements IModPlugin
{
    private static final Identifier ID = Utils.resource("jei_plugin");
    private static IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid()
    {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime)
    {
        ControllableJeiPlugin.runtime = runtime;
    }

    @Override
    public void onRuntimeUnavailable()
    {
        ControllableJeiPlugin.runtime = null;
    }

    public static IJeiRuntime getRuntime()
    {
        return runtime;
    }
}