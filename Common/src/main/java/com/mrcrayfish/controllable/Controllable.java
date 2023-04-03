package com.mrcrayfish.controllable;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.client.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
