package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.RumbleHandler;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;

import org.jetbrains.annotations.Nullable;
import java.io.File;

public class Controllable
{
    private static ControllerManager manager;
    private static File configFolder;
    private static boolean jeiLoaded;
    private static boolean emiLoaded;
    private static boolean reiLoaded;
    private static RumbleHandler rumbleHandler;

    public static void init()
    {
        configFolder = com.mrcrayfish.framework.platform.Services.CONFIG.getConfigPath().toFile();
        jeiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("jei");
        emiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("emi");
        reiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("roughlyenoughitems");
        ControllerProperties.load(configFolder);
        getManager().init();
        rumbleHandler = new RumbleHandler();
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
        // EMI creates a stub JEI, so we need to do this to prevent a crash
        return jeiLoaded && !emiLoaded && !reiLoaded;
    }

    public static boolean isEmiLoaded()
    {
        return emiLoaded;
    }

    public static boolean isReiLoaded()
    {
        return reiLoaded;
    }

    public static RumbleHandler getRumbleHandler()
    {
        return rumbleHandler;
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
            manager = Config.CLIENT.inputLibrary.get().createManager();
        }
        return manager;
    }
}
