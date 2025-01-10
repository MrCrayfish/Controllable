package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.CameraHandler;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.RadialMenu;
import com.mrcrayfish.controllable.client.VirtualCursor;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.input.glfw.GLFWControllerManager;
import com.mrcrayfish.controllable.client.input.sdl2.SDL2ControllerManager;
import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;
import java.io.File;

public class Controllable
{
    private static final VirtualCursor CURSOR = new VirtualCursor();
    private static final InputProcessor INPUT_PROCESSOR = new InputProcessor();
    private static final CameraHandler CAMERA_HANDLER = new CameraHandler();
    private static final RadialMenu RADIAL_MENU = new RadialMenu();

    private static ControllerManager manager;
    private static File configFolder;
    private static boolean jeiLoaded;

    public static void init()
    {
        CURSOR.registerEvents();
        INPUT_PROCESSOR.registerEvents();
        CAMERA_HANDLER.registerEvents();
        RADIAL_MENU.registerEvents();
        configFolder = com.mrcrayfish.framework.platform.Services.CONFIG.getConfigPath().toFile();
        jeiLoaded = com.mrcrayfish.framework.platform.Services.PLATFORM.isModLoaded("jei");
        ControllerProperties.load(configFolder);
        getManager().init();
    }

    public static VirtualCursor getCursor()
    {
        return CURSOR;
    }

    public static InputProcessor getInputProcessor()
    {
        return INPUT_PROCESSOR;
    }

    public static InputHandler getInput()
    {
        return INPUT_PROCESSOR.getHandler();
    }

    public static RadialMenu getRadialMenu()
    {
        return RADIAL_MENU;
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
