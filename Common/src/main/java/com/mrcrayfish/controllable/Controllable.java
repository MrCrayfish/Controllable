package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;

import javax.annotation.Nullable;
import java.io.File;

public class Controllable
{
    private static File configFolder;
    private static boolean jeiLoaded;

    public static void init()
    {
        configFolder = com.mrcrayfish.framework.platform.Services.CONFIG.getConfigPath().toFile();
        jeiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("jei");
        ControllerProperties.load(configFolder);
    }

    public static ControllerInput getInput()
    {
        return InputProcessor.get().getInput();
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
        return InputProcessor.get().getController();
    }

    public static void setController(@Nullable Controller controller)
    {
        InputProcessor.get().setController(controller);
    }
}
