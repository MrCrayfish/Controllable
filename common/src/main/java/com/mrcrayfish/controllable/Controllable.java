package com.mrcrayfish.controllable;

import com.google.common.base.Suppliers;
import com.mrcrayfish.controllable.client.CameraHandler;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.RadialMenu;
import com.mrcrayfish.controllable.client.RumbleHandler;
import com.mrcrayfish.controllable.client.ScrollingHandler;
import com.mrcrayfish.controllable.client.VirtualCursor;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.glfw.GLFWControllerManager;
import com.mrcrayfish.controllable.client.input.sdl2.SDL2ControllerManager;
import com.mrcrayfish.controllable.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Controllable
{
    private static final Supplier<AdaptiveControllerManager> MANAGER = Suppliers.memoize(Controllable::createManager);
    private static final InputHandler INPUT_HANDLER = new InputHandler();
    private static final BindingRegistry BINDING_REGISTRY = new BindingRegistry();
    private static final VirtualCursor CURSOR = new VirtualCursor();
    private static final InputProcessor INPUT_PROCESSOR = new InputProcessor();
    private static final CameraHandler CAMERA_HANDLER = new CameraHandler();
    private static final RadialMenu RADIAL_MENU = new RadialMenu();
    private static final ScrollingHandler SCROLLING_HANDLER = new ScrollingHandler();
    private static final RumbleHandler RUMBLE_HANDLER = new RumbleHandler();

    private static final boolean ARCHITECTURY_LOADED = Utils.isModLoaded("architectury");
    private static final boolean EMI_LOADED = Utils.isModLoaded("emi");
    private static final boolean REI_LOADED = Utils.isModLoaded("roughlyenoughitems");
    private static final boolean JEI_LOADED = Utils.isModLoaded("jei") && !EMI_LOADED && !REI_LOADED;
    private static final boolean TACZ_LOADED = Utils.isModLoaded("tacz");

    public static void init()
    {
        MANAGER.get().init();
        INPUT_PROCESSOR.registerEvents();
        CURSOR.registerEvents();
        INPUT_HANDLER.registerEvents();
        CAMERA_HANDLER.registerEvents();
        RADIAL_MENU.registerEvents();
        SCROLLING_HANDLER.registerEvents();
        
        // DEBUG: Print all registered bindings
        System.out.println("[Controllable] === Registered Bindings ===");
        BINDING_REGISTRY.getBindings().forEach(binding -> {
            System.out.println("[Controllable] " + binding.getDescription() + " -> Button: " + binding.getButton() + " (Multi: " + binding.isMultiButton() + ")");
        });
        System.out.println("[Controllable] === End Bindings ===");
    }

    public static BindingRegistry getBindingRegistry()
    {
        return BINDING_REGISTRY;
    }

    public static VirtualCursor getCursor()
    {
        return CURSOR;
    }

    public static InputProcessor getInputProcessor()
    {
        return INPUT_PROCESSOR;
    }

    public static InputHandler getInputHandler()
    {
        return INPUT_HANDLER;
    }

    public static RadialMenu getRadialMenu()
    {
        return RADIAL_MENU;
    }

    public static RumbleHandler getRumbleHandler()
    {
        return RUMBLE_HANDLER;
    }

    public static boolean isTaczLoaded()
    {
        return TACZ_LOADED;
    }
    
    public static boolean isArchitecturyLoaded()
    {
        return ARCHITECTURY_LOADED;
    }

    public static boolean isJeiLoaded()
    {
        return JEI_LOADED;
    }

    public static boolean isEmiLoaded()
    {
        return EMI_LOADED;
    }

    public static boolean isReiLoaded()
    {
        return REI_LOADED;
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
        return switch (Config.CLIENT.inputLibrary.get()) {
            case GLFW -> new GLFWControllerManager();
            case SDL2 -> new SDL2ControllerManager();
        };
    }
}
