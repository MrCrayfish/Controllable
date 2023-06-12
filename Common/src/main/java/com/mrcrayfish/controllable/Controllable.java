package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.input.glfw.GLFWControllerManager;
import com.mrcrayfish.controllable.client.input.sdl2.SDL2ControllerManager;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.File;

public class Controllable
{
    private static ControllerManager manager;
    private static File configFolder;
    private static boolean jeiLoaded;

    public static void init()
    {
        configFolder = com.mrcrayfish.framework.platform.Services.CONFIG.getConfigPath().toFile();
        jeiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("jei");
        ControllerProperties.load(configFolder);
        getManager().init();
    }

    public static ControllerInput getInput()
    {
        return InputProcessor.instance().getInput();
    }

    public static File getConfigFolder()
    {
        return configFolder;
    }

    public static boolean isJeiLoaded()
    {
        return jeiLoaded;
    }

    @Nullable
    public static Controller getController()
    {
        return getManager().getActiveController();
    }

    public static ControllerManager getManager()
    {
        if(manager == null)
        {
            if(!Minecraft.ON_OSX)
            {
                manager = new SDL2ControllerManager();
            }
            else
            {
                manager = new GLFWControllerManager();
            }
        }
        return manager;
    }
}
