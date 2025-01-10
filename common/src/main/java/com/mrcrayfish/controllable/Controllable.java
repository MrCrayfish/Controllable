package com.mrcrayfish.controllable;

import com.google.common.base.Suppliers;
import com.mrcrayfish.controllable.client.CameraHandler;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.MovementHandler;
import com.mrcrayfish.controllable.client.RadialMenu;
import com.mrcrayfish.controllable.client.VirtualCursor;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.util.Utils;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Controllable
{
    private static final Supplier<AdaptiveControllerManager> MANAGER = Suppliers.memoize(Controllable::createManager);
    private static final ControllerProperties PROPERTIES = new ControllerProperties();
    private static final VirtualCursor CURSOR = new VirtualCursor();
    private static final InputProcessor INPUT_PROCESSOR = new InputProcessor();
    private static final CameraHandler CAMERA_HANDLER = new CameraHandler();
    private static final RadialMenu RADIAL_MENU = new RadialMenu();
    private static final MovementHandler MOVEMENT_HANDLER = new MovementHandler();

    private static final boolean JEI_LOADED = Utils.isModLoaded("jei");

    public static void init()
    {
        PROPERTIES.load();
        MANAGER.get().init();
        CURSOR.registerEvents();
        INPUT_PROCESSOR.registerEvents();
        CAMERA_HANDLER.registerEvents();
        RADIAL_MENU.registerEvents();
        MOVEMENT_HANDLER.registerEvents();
    }

    public static VirtualCursor getCursor()
    {
        return CURSOR;
    }

    public static InputProcessor getInputProcessor()
    {
        return INPUT_PROCESSOR;
    }

    // TODO this should probably not be a thing soon
    public static InputHandler getInput()
    {
        return INPUT_PROCESSOR.getHandler();
    }

    public static RadialMenu getRadialMenu()
    {
        return RADIAL_MENU;
    }

    public static boolean isJeiLoaded()
    {
        return JEI_LOADED;
    }

    public static AdaptiveControllerManager getControllerManager()
    {
        return MANAGER.get();
    }

    @Nullable
    public static Controller getController()
    {
        return MANAGER.get().getActiveController();
    }

    private static AdaptiveControllerManager createManager()
    {
        return Config.CLIENT.inputLibrary.get().createManager();
    }
}
