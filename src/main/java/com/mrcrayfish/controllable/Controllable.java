package com.mrcrayfish.controllable;

import com.badlogic.gdx.controllers.ControllerAdapter;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.ButtonBindingScreen;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
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

    private static SDL2ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;
    private static File configFolder;

    public Controllable()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    public static ControllerInput getInput()
    {
        return input;
    }

    public static File getConfigFolder()
    {
        return configFolder;
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        Minecraft mc = event.getMinecraftSupplier().get();
        configFolder = new File(mc.gameDir, "config");

        ControllerProperties.load(configFolder);

        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new SDL2ControllerManager();
        Controllable.manager.addListenerAndRunForConnectedControllers(this);

        /* Attempts to load the first controller connected if auto select is enabled */
        if(Config.CLIENT.options.autoSelect.get() && manager.getControllers().size > 0)
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
        MinecraftForge.EVENT_BUS.register(new ControllerEvents());

        this.startControllerThread();
    }

    private void onLoadComplete(FMLLoadCompleteEvent event)
    {
        if(FMLLoader.getDist() != Dist.CLIENT)
            return;

        BindingRegistry.getInstance().load();
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
                    if(Config.CLIENT.options.autoSelect.get())
                    {
                        setController((SDL2Controller) sdlController);
                    }

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

                    if(Config.CLIENT.options.autoSelect.get() && manager.getControllers().size > 0)
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

    private void startControllerThread()
    {
        Runnable r = () ->
        {
            final long pollInterval = Config.CLIENT.controllerPollInterval.get();
            while(Minecraft.getInstance().isRunning())
            {
                try
                {
                    manager.pollState();
                    this.gatherAndQueueControllerInput();
                    try
                    {
                        Thread.sleep(pollInterval);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(SDL_Error e)
                {
                    e.printStackTrace();
                }
            }
            manager.close();
        };
        Thread controllerPollThread = new Thread(r, "Controller Input");
        controllerPollThread.setDaemon(true);
        controllerPollThread.start();
    }
    
    private void gatherAndQueueControllerInput()
    {
        Controller currentController = controller;
        if(currentController == null)
            return;
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, this.getButtonState(SDL_CONTROLLER_BUTTON_A));
        states.setState(Buttons.B, this.getButtonState(SDL_CONTROLLER_BUTTON_B));
        states.setState(Buttons.X, this.getButtonState(SDL_CONTROLLER_BUTTON_X));
        states.setState(Buttons.Y, this.getButtonState(SDL_CONTROLLER_BUTTON_Y));
        states.setState(Buttons.SELECT, this.getButtonState(SDL_CONTROLLER_BUTTON_BACK));
        states.setState(Buttons.HOME, this.getButtonState(SDL_CONTROLLER_BUTTON_GUIDE));
        states.setState(Buttons.START, this.getButtonState(SDL_CONTROLLER_BUTTON_START));
        states.setState(Buttons.LEFT_THUMB_STICK, this.getButtonState(SDL_CONTROLLER_BUTTON_LEFTSTICK));
        states.setState(Buttons.RIGHT_THUMB_STICK, this.getButtonState(SDL_CONTROLLER_BUTTON_RIGHTSTICK));
        states.setState(Buttons.LEFT_BUMPER, this.getButtonState(SDL_CONTROLLER_BUTTON_LEFTSHOULDER));
        states.setState(Buttons.RIGHT_BUMPER, this.getButtonState(SDL_CONTROLLER_BUTTON_RIGHTSHOULDER));
        states.setState(Buttons.LEFT_TRIGGER, Math.abs(currentController.getLTriggerValue()) >= 0.1F);
        states.setState(Buttons.RIGHT_TRIGGER, Math.abs(currentController.getRTriggerValue()) >= 0.1F);
        states.setState(Buttons.DPAD_UP, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_RIGHT));
        Minecraft.getInstance().enqueue(() -> this.processButtons(states));
    }

    private void processButtons(ButtonStates states)
    {
        ButtonBinding.tick();
        for(int i = 0; i < Buttons.BUTTONS.length; i++)
        {
            this.processButton(Buttons.BUTTONS[i], states);
        }
    }

    private void processButton(int index, ButtonStates newStates)
    {
        boolean state = newStates.getState(index);

        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof ControllerLayoutScreen)
        {
            ((ControllerLayoutScreen) screen).processButton(index, newStates);
            return;
        }

        if (controller == null)
        {
            return;
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
                if(screen instanceof ButtonBindingScreen)
                {
                    if(((ButtonBindingScreen) screen).processButton(index))
                    {
                        return;
                    }
                }
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
