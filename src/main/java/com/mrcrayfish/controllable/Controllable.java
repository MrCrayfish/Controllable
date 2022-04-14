package com.mrcrayfish.controllable;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.ButtonBindingScreen;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
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
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
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
            Minecraft mc = Minecraft.getInstance();
            configFolder = new File(mc.gameDirectory, "config");
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

            Mappings.load(configFolder);

            /* Registers events */
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(input = new ControllerInput());
            MinecraftForge.EVENT_BUS.register(new RenderEvents());
            MinecraftForge.EVENT_BUS.register(new ScreenEvents(Controllable.manager));
            MinecraftForge.EVENT_BUS.register(new ControllerEvents());
            MinecraftForge.EVENT_BUS.register(RadialMenuHandler.instance());
            MinecraftForge.EVENT_BUS.addListener(this::controllerTick);
        });
    }

    @Override
    public void connected(int jid)
    {
        Minecraft.getInstance().doRunTask(() ->
        {
            if(Controllable.controller == null)
            {
                if(Config.CLIENT.options.autoSelect.get())
                {
                    setController(new Controller(jid));
                }

                Minecraft mc = Minecraft.getInstance();
                if(mc.player != null && Controllable.controller != null)
                {
                    Minecraft.getInstance().getToasts().addToast(new ControllerToast(true, Controllable.controller.getName()));
                }
            }
        });
    }

    @Override
    public void disconnected(int jid)
    {
        Minecraft.getInstance().doRunTask(() ->
        {
            if(Controllable.controller != null)
            {
                if(Controllable.controller.getJid() == jid)
                {
                    Controller oldController = Controllable.controller;

                    setController(null);

                    if(Config.CLIENT.options.autoSelect.get() && manager.getControllerCount() > 0)
                    {
                        Optional<Integer> optional = manager.getControllers().keySet().stream().min(Comparator.comparing(i -> i));
                        optional.ifPresent(minJid -> setController(new Controller(minJid)));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null)
                    {
                        Minecraft.getInstance().getToasts().addToast(new ControllerToast(false, oldController.getName()));
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

    private void controllerTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        if(manager != null)
        {
            manager.update();
        }
        if(controller != null)
        {
            controller.updateGamepadState();
            gatherAndQueueControllerInput();
        }
    }
    
    private static void gatherAndQueueControllerInput()
    {
        Controller currentController = controller;
        if(currentController == null)
            return;
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_A));
        states.setState(Buttons.B, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_B));
        states.setState(Buttons.X, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_X));
        states.setState(Buttons.Y, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_Y));
        states.setState(Buttons.SELECT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_BACK));
        states.setState(Buttons.HOME, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE));
        states.setState(Buttons.START, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_START));
        states.setState(Buttons.LEFT_THUMB_STICK, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB));
        states.setState(Buttons.RIGHT_THUMB_STICK, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB));
        states.setState(Buttons.LEFT_BUMPER, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER));
        states.setState(Buttons.RIGHT_BUMPER, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER));
        states.setState(Buttons.LEFT_TRIGGER, currentController.getLTriggerValue() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, currentController.getRTriggerValue() >= 0.5F);
        states.setState(Buttons.DPAD_UP, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT));
        processButtonStates(states);
    }

    private static void processButtonStates(ButtonStates states)
    {
        ButtonBinding.tick();
        for(int i = 0; i < Buttons.BUTTONS.length; i++)
        {
            processButton(Buttons.BUTTONS[i], states);
        }
    }

    private static void processButton(int index, ButtonStates newStates)
    {
        boolean state = newStates.getState(index);

        Screen screen = Minecraft.getInstance().screen;
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
        return controller != null && controller.getButtonsStates().getState(button);
    }

    private static boolean getButtonState(int buttonCode)
    {
        return controller != null && controller.getGamepadState().buttons(buttonCode) == GLFW.GLFW_PRESS;
    }
}
