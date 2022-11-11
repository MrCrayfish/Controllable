package com.mrcrayfish.controllable;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.ButtonStateTracker.ButtonStateChange;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerEvents;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.ControllerManager;
import com.mrcrayfish.controllable.client.ControllerPoller;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.ControllerToast;
import com.mrcrayfish.controllable.client.GuiEvents;
import com.mrcrayfish.controllable.client.IControllerListener;
import com.mrcrayfish.controllable.client.Mappings;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.RenderEvents;
import com.mrcrayfish.controllable.client.gui.ButtonBindingScreen;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Controllable implements IControllerListener
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static ControllerPoller poller;
    private static ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;
    private static File configFolder;
    private static boolean jeiLoaded;

    public Controllable()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
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

    public static boolean isJeiLoaded()
    {
        return jeiLoaded;
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            Minecraft mc = event.getMinecraftSupplier().get();
            configFolder = new File(mc.gameDir, "config");
            jeiLoaded = ModList.get().isLoaded("jei");

            ControllerProperties.load(configFolder);

            try(InputStream is = Mappings.class.getResourceAsStream("/gamecontrollerdb.txt"))
            {
                if(is != null)
                {
                    byte[] bytes = ByteStreams.toByteArray(is);
                    ByteBuffer buffer = MemoryUtil.memASCIISafe(new String(bytes));
                    if(buffer != null && GLFW.glfwUpdateGamepadMappings(buffer))
                    {
                        LOGGER.info("Successfully updated gamepad mappings");
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            /* Loads up the controller manager and adds a listener */
            Controllable.manager = new ControllerManager();
            Controllable.manager.addControllerListener(this);

            /* Attempts to load the first controller connected if auto select is enabled */
            if(Config.CLIENT.options.autoSelect.get())
            {
                if(GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1) && GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1))
                {
                    setController(new Controller(GLFW.GLFW_JOYSTICK_1));
                }
            }

            Controllable.poller = new ControllerPoller(Controllable.manager);
            Controllable.poller.startAsyncPolling();

            Mappings.load(configFolder);

            /* Registers events */
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(input = new ControllerInput());
            MinecraftForge.EVENT_BUS.register(new RenderEvents());
            MinecraftForge.EVENT_BUS.register(new GuiEvents(Controllable.manager));
            MinecraftForge.EVENT_BUS.register(new ControllerEvents());
            MinecraftForge.EVENT_BUS.register(RadialMenuHandler.instance());
            MinecraftForge.EVENT_BUS.addListener(this::controllerTick);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void connected(int jid)
    {
        Preconditions.checkState(Minecraft.getInstance().isOnExecutionThread(),
                "connected should be run on main thread");
        if(Controllable.controller == null && Config.CLIENT.options.autoSelect.get())
        {
            Controller newController = new Controller(jid);
            setController(newController);

            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null)
            {
                Minecraft.getInstance().getToastGui().add(new ControllerToast(true, newController.getName()));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void disconnected(int jid)
    {
        Preconditions.checkState(Minecraft.getInstance().isOnExecutionThread(),
                "disconnected should be run on main thread");
        Controller oldController = Controllable.controller;
        if(oldController != null && oldController.getJid() == jid)
        {
            setController(null);

            if(Config.CLIENT.options.autoSelect.get() && manager.getControllerCount() > 0)
            {
                Optional<Integer> optional = manager.getControllers().keySet().stream().min(Comparator.comparing(i -> i));
                optional.ifPresent(minJid -> setController(new Controller(minJid)));
            }

            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null)
            {
                Minecraft.getInstance().getToastGui().add(new ControllerToast(false, oldController.getName()));
            }
        }
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

    private void controllerTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        if(!poller.isAsyncPollingEnabled())
        {
            poller.poll();
        }
        Controller controller = Controllable.controller;
        ButtonStateTracker stateTracker = poller.getStateTracker(controller);
        if(stateTracker != null)
        {
            processButtonStates(stateTracker, controller);
        }
    }
    
    private static void processButtonStates(ButtonStateTracker stateTracker, Controller controller)
    {
        ButtonBinding.tick();

        synchronized(stateTracker)
        {
            boolean[] changedButtons = new boolean[Buttons.LENGTH];
            ButtonStateChange change;
            while ((change = stateTracker.poll()) != null)
            {
                processButton(change.button, change.state, controller);
                changedButtons[change.button] = true;
            }
            ButtonStates states = stateTracker.getLastState();
            for (int button = 0; button < changedButtons.length; button++) {
                if(!changedButtons[button])
                {
                    processButton(button, states.getState(button), controller);
                }
            }
        }
    }

    private static void processButton(int index, boolean state, Controller controller)
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof ControllerLayoutScreen)
        {
            ((ControllerLayoutScreen) screen).processButton(index, state);
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
                input.handleButtonInput(controller, index, true, false);
            }
        }
        else if(states.getState(index))
        {
            states.setState(index, false);
            input.handleButtonInput(controller, index, false, false);
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
        Controller c = controller;
        return c != null && c.getButtonsStates().getState(button);
    }


}
