package com.mrcrayfish.controllable;

import com.badlogic.gdx.controllers.ControllerAdapter;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.libsdl.SDL_Error;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import javax.annotation.Nullable;
import java.io.File;

import static org.libsdl.SDL.*;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Controllable extends ControllerAdapter
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static ControllerOptions options;
    private static SDL2ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;

    public Controllable()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    public static ControllerOptions getOptions()
    {
        return options;
    }

    public static ControllerInput getInput()
    {
        return input;
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new SDL2ControllerManager();
        Controllable.manager.addListenerAndRunForConnectedControllers(this);

        Minecraft mc = event.getMinecraftSupplier().get();
        File configFolder = new File(mc.gameDir, "config");

        ControllerProperties.load(configFolder);
        Controllable.options = new ControllerOptions(mc.gameDir);

        /* Attempts to load the first controller connected if auto select is enabled */
        if(options.isAutoSelect() && manager.getControllers().size > 0)
        {
            com.badlogic.gdx.controllers.Controller controller = manager.getControllers().get(0);
            if(controller instanceof SDL2Controller)
            {
                setController((SDL2Controller) controller);
            }
        }

        Mappings.load(configFolder);

        /* Registers events */
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(input = new ControllerInput());
        MinecraftForge.EVENT_BUS.register(new RenderEvents());
        MinecraftForge.EVENT_BUS.register(new GuiEvents(Controllable.manager));
    }

    @Override
    public void connected(com.badlogic.gdx.controllers.Controller sdlController)
    {
        Minecraft.getInstance().enqueue(() ->
        {
            if(sdlController instanceof SDL2Controller)
            {
                if(Controllable.controller == null)
                {
                    setController((SDL2Controller) sdlController);

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null)
                    {
                        Minecraft.getInstance().getToastGui().add(new ControllerToast(true, Controllable.controller.getName()));
                    }
                }
            }
        });
    }

    @Override
    public void disconnected(com.badlogic.gdx.controllers.Controller sdlController)
    {
        Minecraft.getInstance().enqueue(() ->
        {
            if(Controllable.controller != null)
            {
                if(Controllable.controller.getSDL2Controller() == sdlController)
                {
                    Controller oldController = Controllable.controller;

                    setController(null);

                    if(options.isAutoSelect() && manager.getControllers().size > 0)
                    {
                        setController((SDL2Controller) manager.getControllers().get(0));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null)
                    {
                        Minecraft.getInstance().getToastGui().add(new ControllerToast(false, oldController.getName()));
                    }
                }
            }
        });
    }

    public static void setController(@Nullable SDL2Controller sdl2Controller)
    {
        if(sdl2Controller != null)
        {
            Controller controller = new Controller(sdl2Controller);
            Controllable.controller = controller;
            Mappings.updateControllerMappings(controller);
        }
        else
        {
            Controllable.controller = null;
        }
    }

    @SubscribeEvent
    public void handleButtonInput(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        try
        {
            manager.pollState();
        }
        catch(SDL_Error e)
        {
            e.printStackTrace();
        }

        if(controller == null)
            return;

        ButtonBinding.tick();

        this.processButton(Buttons.A, getButtonState(SDL_CONTROLLER_BUTTON_A));
        this.processButton(Buttons.B, getButtonState(SDL_CONTROLLER_BUTTON_B));
        this.processButton(Buttons.X, getButtonState(SDL_CONTROLLER_BUTTON_X));
        this.processButton(Buttons.Y, getButtonState(SDL_CONTROLLER_BUTTON_Y));
        this.processButton(Buttons.SELECT, getButtonState(SDL_CONTROLLER_BUTTON_BACK));
        this.processButton(Buttons.HOME, getButtonState(SDL_CONTROLLER_BUTTON_GUIDE));
        this.processButton(Buttons.START, getButtonState(SDL_CONTROLLER_BUTTON_START));
        this.processButton(Buttons.LEFT_THUMB_STICK, getButtonState(SDL_CONTROLLER_BUTTON_LEFTSTICK));
        this.processButton(Buttons.RIGHT_THUMB_STICK, getButtonState(SDL_CONTROLLER_BUTTON_RIGHTSTICK));
        this.processButton(Buttons.LEFT_BUMPER, getButtonState(SDL_CONTROLLER_BUTTON_LEFTSHOULDER));
        this.processButton(Buttons.RIGHT_BUMPER, getButtonState(SDL_CONTROLLER_BUTTON_RIGHTSHOULDER));
        this.processButton(Buttons.LEFT_TRIGGER, Math.abs(controller.getLTriggerValue()) >= 0.1F);
        this.processButton(Buttons.RIGHT_TRIGGER, Math.abs(controller.getRTriggerValue()) >= 0.1F);
        this.processButton(Buttons.DPAD_UP, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_UP));
        this.processButton(Buttons.DPAD_DOWN, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_DOWN));
        this.processButton(Buttons.DPAD_LEFT, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_LEFT));
        this.processButton(Buttons.DPAD_RIGHT, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_RIGHT));
    }

    private void processButton(int index, boolean state)
    {
        if(Minecraft.getInstance().currentScreen instanceof ControllerLayoutScreen && state)
        {
            if(((ControllerLayoutScreen) Minecraft.getInstance().currentScreen).onButtonInput(index))
            {
                return;
            }
        }

        if(controller.getMapping() != null)
        {
            index = controller.getMapping().remap(index);
        }

        //No binding so don't perform any action
        if(index == -1)
        {
            return;
        }

        ButtonStates states = controller.getButtonsStates();

        if(state)
        {
            if(!states.getState(index))
            {
                states.setState(index, true);
                input.handleButtonInput(controller, index, true);
            }
        }
        else if(states.getState(index))
        {
            states.setState(index, false);
            input.handleButtonInput(controller, index, false);
        }
    }

    /**
     * Returns whether a button on the controller is pressed or not. This is a raw approach to
     * getting whether a button is pressed or not. You should use a {@link ButtonBinding} instead.
     *
     * @param button the button to check if pressed
     * @return
     */
    public static boolean isButtonPressed(int button)
    {
        return controller != null && controller.getButtonsStates().getState(button);
    }

    private boolean getButtonState(int buttonCode)
    {
        return controller != null && controller.getSDL2Controller().getButton(buttonCode);
    }
}
