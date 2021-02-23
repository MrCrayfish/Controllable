package com.mrcrayfish.controllable;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.asm.ControllablePlugin;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.ButtonBindingScreen;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import static org.libsdl.SDL.*;

/**
 * Author: MrCrayfish
 */
//@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, clientSideOnly = true, certificateFingerprint = "4d54165f7f65cf475bf13341569655b980a5b430")
public class Controllable extends DummyModContainer implements IControllerListener
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static ControllerOptions options;
    private static ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;
    private static File configFolder;

    public Controllable()
    {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = Reference.MOD_ID;
        meta.name = Reference.MOD_NAME;
        meta.version = Reference.MOD_VERSION;
        meta.logoFile = "controllable.png";
        meta.authorList = Collections.singletonList("MrCrayfish");
        meta.updateJSON = "https://raw.githubusercontent.com/MrCrayfish/Controllable/master/update.json";
        meta.url = "https://mrcrayfish.com/mod?id=controllable";
        meta.description = "Adds in the ability to use a controller to play Minecraft";
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
        return ControllablePlugin.location;
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

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    public static ControllerInput getInput()
    {
        return input;
    }

    public static ControllerManager getManager()
    {
        return manager;
    }

    public static ControllerOptions getOptions()
    {
        return options;
    }

    public static File getConfigFolder()
    {
        return configFolder;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        configFolder = new File(mc.gameDir, "config");

        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new ControllerManager();
        Controllable.manager.addControllerListener(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Controllable.manager.close()));

        ControllerProperties.load(event.getModConfigurationDirectory());

        Controllable.options = new ControllerOptions(mc, mc.gameDir);

        /* Attempts to load the first controller connected */
        if(options.isAutoSelect())
        {
            int jid = manager.getFirstControllerJid();
            if(jid != -1)
            {
                setController(new Controller(jid));
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

    @Subscribe
    public void onLoadComplete(FMLLoadCompleteEvent event)
    {
        BindingRegistry.getInstance().load();
    }

    @Override
    public void connected(int jid)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            if(Controllable.controller == null)
            {
                if(Controllable.getOptions().isAutoSelect())
                {
                    setController(new Controller(jid));
                }

                Minecraft mc = Minecraft.getMinecraft();
                if(mc.player != null && Controllable.controller != null)
                {
                    Minecraft.getMinecraft().getToastGui().add(new ControllerToast(true, Controllable.controller.getName()));
                }
            }
        });
    }

    @Override
    public void disconnected(int jid)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            if(Controllable.controller != null)
            {
                if(Controllable.controller.getJid() == jid)
                {
                    Controller oldController = Controllable.controller;

                    setController(null);

                    if(Controllable.getOptions().isAutoSelect() && manager.getControllerCount() > 0)
                    {
                        Optional<Integer> optional = manager.getControllers().keySet().stream().min(Comparator.comparing(i -> i));
                        optional.ifPresent(minJid -> setController(new Controller(minJid)));
                    }

                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.player != null)
                    {
                        Minecraft.getMinecraft().getToastGui().add(new ControllerToast(false, oldController.getName()));
                    }
                }
            }
        });
    }

    public static void setController(@Nullable Controller controller)
    {
        if(controller != null)
        {
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
            final long pollInterval = ControllerProperties.getPollRate();
            while(true)
            {
                manager.update();

                if(controller != null)
                {
                    this.gatherAndQueueControllerInput();
                }
                try
                {
                    Thread.sleep(pollInterval);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
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
        states.setState(Buttons.LEFT_TRIGGER, currentController.getLTriggerValue() > 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, currentController.getRTriggerValue() > 0.5F);
        states.setState(Buttons.DPAD_UP, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, this.getButtonState(SDL_CONTROLLER_BUTTON_DPAD_RIGHT));
        Minecraft.getMinecraft().addScheduledTask(() -> this.processButtons(states));
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

        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if(screen instanceof ControllerLayoutScreen)
        {
            ((ControllerLayoutScreen) screen).processButton(index, newStates);
            return;
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
