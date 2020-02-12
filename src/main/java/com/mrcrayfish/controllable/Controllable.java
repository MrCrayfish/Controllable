package com.mrcrayfish.controllable;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.asm.ControllablePlugin;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.GuiControllerLayout;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.libsdl.SDL_Error;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;

import static org.libsdl.SDL.*;

/**
 * Author: MrCrayfish
 */
//@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MOD_COMPATIBILITY, clientSideOnly = true, certificateFingerprint = "4d54165f7f65cf475bf13341569655b980a5b430")
public class Controllable extends DummyModContainer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static ControllerOptions options;
    private static SDL2ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;
    private static boolean[] buttonStates;

    public Controllable()
    {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = Reference.MOD_ID;
        meta.name = Reference.MOD_NAME;
        meta.version = Reference.MOD_VERSION;
        meta.authorList = Collections.singletonList("MrCrayfish");
        meta.url = "https://mrcrayfish.com/mod?id=controllable";
    }

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    @Override
    public boolean registerBus(final EventBus bus, final LoadController controller)
    {
        bus.register(this);
        return true;
    }

    @Override
    public File getSource()
    {
        return ControllablePlugin.LOCATION;
    }

    @Override
    public boolean shouldLoadInEnvironment()
    {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    @Override
    public Class<?> getCustomResourcePackClass()
    {
        return this.getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }

    public static ControllerOptions getOptions()
    {
        return options;
    }

    public static ControllerInput getInput()
    {
        return input;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event)
    {
        //ControllerProperties.load(event.getModConfigurationDirectory());

        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new SDL2ControllerManager();
        Controllable.manager.addListenerAndRunForConnectedControllers(new ControllerHandler());

        ControllerProperties.load(event.getModConfigurationDirectory());

        Minecraft mc = Minecraft.getMinecraft();
        Controllable.options = new ControllerOptions(mc, mc.gameDir);

        /* Attempts to load the first controller connected */
        if(options.isAutoSelect() && manager.getControllers().size > 0)
        {
            com.badlogic.gdx.controllers.Controller controller = manager.getControllers().get(0);
            if(controller instanceof SDL2Controller)
            {
                setController((SDL2Controller) controller);
            }
        }

        Mappings.load(event.getModConfigurationDirectory());

        /* Registers events */
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(input = new ControllerInput());
        MinecraftForge.EVENT_BUS.register(new RenderEvents());
        MinecraftForge.EVENT_BUS.register(new GuiEvents(Controllable.manager));
        MinecraftForge.EVENT_BUS.register(new ControllerEvents());
    }



    public static void setController(@Nullable SDL2Controller sdl2Controller)
    {
        if(sdl2Controller != null)
        {
            Controllable.controller = new Controller(sdl2Controller);
            buttonStates = new boolean[Buttons.LENGTH];
            Mappings.updateControllerMappings(Controllable.controller);
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

        Controller currentController = controller;
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
        this.processButton(Buttons.LEFT_TRIGGER, Math.abs(currentController.getLTriggerValue()) >= 0.1F);
        this.processButton(Buttons.RIGHT_TRIGGER, Math.abs(currentController.getRTriggerValue()) >= 0.1F);
        this.processButton(Buttons.DPAD_UP, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_UP));
        this.processButton(Buttons.DPAD_DOWN, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_DOWN));
        this.processButton(Buttons.DPAD_LEFT, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_LEFT));
        this.processButton(Buttons.DPAD_RIGHT, getButtonState(SDL_CONTROLLER_BUTTON_DPAD_RIGHT));
    }

    private void processButton(int index, boolean state)
    {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiControllerLayout && state)
        {
            if(((GuiControllerLayout) Minecraft.getMinecraft().currentScreen).onButtonInput(index))
            {
                return;
            }
        }

        if(controller == null)
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

    private static boolean getButtonState(int buttonCode)
    {
        return controller != null && controller.getSDL2Controller().getButton(buttonCode);
    }

    private static class ControllerHandler implements ControllerListener
    {
        @Override
        public void connected(com.badlogic.gdx.controllers.Controller sdlController)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                if(sdlController instanceof SDL2Controller)
                {
                    if(Controllable.controller == null)
                    {
                        if(options.isAutoSelect())
                        {
                            setController((SDL2Controller) sdlController);
                        }

                        Minecraft mc = Minecraft.getMinecraft();
                        if(mc.player != null)
                        {
                            Minecraft.getMinecraft().getToastGui().add(new ControllerToast(true, controller.getName()));
                        }
                    }
                }
            });
        }

        @Override
        public void disconnected(com.badlogic.gdx.controllers.Controller sdlController)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                if(Controllable.controller != null)
                {
                    if(Controllable.controller.getSDL2Controller() == sdlController)
                    {
                        setController(null);

                        if(options.isAutoSelect() && manager.getControllers().size > 0)
                        {
                            setController((SDL2Controller) manager.getControllers().get(0));
                        }

                        Minecraft mc = Minecraft.getMinecraft();
                        if(mc.player != null)
                        {
                            Minecraft.getMinecraft().getToastGui().add(new ControllerToast(false, sdlController.getName()));
                        }
                    }
                }
            });
        }
    }
}
