package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Controllable
{//4d54165f7f65cf475bf13341569655b980a5b430
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static ControllerManager manager;
    private static Controller controller;
    private static ControllerInput input;
    private static boolean[] buttonStates;
    private static int selectedControllerIndex;
    private static List<String> connectedControllerNames;
    private static int currentControllerCount;

    public Controllable()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    public static int getSelectedControllerIndex()
    {
        return selectedControllerIndex;
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new ControllerManager();
        Controllable.manager.initSDLGamepad();
        Controllable.currentControllerCount = manager.getNumControllers();
        Controllable.connectedControllerNames = getConnectedControllerNames();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Controllable.manager.quitSDLGamepad()));

        Minecraft mc = event.getMinecraftSupplier().get();
        File configFolder = new File(mc.gameDir, "config");

        ControllerProperties.load(configFolder);

        /* Attempts to load the first controller connected */
        ControllerIndex index = manager.getControllerIndex(0);
        if(index.isConnected())
        {
            setController(new Controller(index));
        }

        Mappings.load(configFolder);

        /* Registers events */
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(input = new ControllerInput());
        MinecraftForge.EVENT_BUS.register(new RenderEvents());
        MinecraftForge.EVENT_BUS.register(new GuiEvents(Controllable.manager));
    }

    public static void setController(Controller controller)
    {
        Controllable.controller = controller;
        selectedControllerIndex = controller.getNumber();
        buttonStates = new boolean[Buttons.LENGTH];
        controller.updateState(manager.getState(selectedControllerIndex));
    }

    public static List<String> getConnectedControllerNames()
    {
        /* Ensures that the number of controllers matches the controller index size */
        manager.update();

        /* Adds all the connected controller's names to a list. Used to determine which controller was
         * connected or disconnected */
        List<String> names = new ArrayList<>();
        for(int i = 0; i < manager.getNumControllers(); i++)
        {
            try
            {
                names.add(manager.getControllerIndex(i).getName());
            }
            catch(ControllerUnpluggedException e)
            {
                e.printStackTrace();
            }
        }
        return names;
    }

    @SubscribeEvent
    public void handleButtonInput(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        manager.update();

        Minecraft mc = Minecraft.getInstance();
        int controllersCount = manager.getNumControllers();
        if(controllersCount != currentControllerCount)
        {
            boolean connected = controllersCount > currentControllerCount;
            List<String> newControllers = connected ? getConnectedControllerNames() : connectedControllerNames;
            List<String> oldControllers = connected ? connectedControllerNames : getConnectedControllerNames();
            for(String name : oldControllers)
            {
                Iterator<String> it = newControllers.iterator();
                while(it.hasNext())
                {
                    if(it.next().equals(name))
                    {
                        it.remove();
                        break;
                    }
                }
            }

            /* If no controllers were plugged in, make the new controller the active one */
            if(currentControllerCount == 0 && controllersCount > 0)
            {
                setController(new Controller(manager.getControllerIndex(controllersCount - 1)));
            }

            connectedControllerNames = getConnectedControllerNames();
            currentControllerCount = manager.getNumControllers();

            if(mc.player != null)
            {
                String controllerName = newControllers.size() > 0 ? newControllers.get(0) : I18n.format("controllable.toast.controller");
                Minecraft.getInstance().getToastGui().add(new ControllerToast(connected, controllerName));
            }
        }

        ControllerState state = manager.getState(selectedControllerIndex);
        if(!state.isConnected)
        {
            if(controller != null)
            {
                controller = null;
            }
            if(selectedControllerIndex >= 0)
            {
                selectedControllerIndex--;
            }
            return;
        }
        else if(controller == null)
        {
            controller = new Controller(manager.getControllerIndex(selectedControllerIndex));
            buttonStates = new boolean[Buttons.LENGTH];
        }

        controller.updateState(state);

        ButtonBinding.tick();

        processButton(Buttons.A, state.a);
        processButton(Buttons.B, state.b);
        processButton(Buttons.X, state.x);
        processButton(Buttons.Y, state.y);
        processButton(Buttons.SELECT, state.back);
        processButton(Buttons.HOME, state.guide);
        processButton(Buttons.START, state.start);
        processButton(Buttons.LEFT_THUMB_STICK, state.leftStickClick);
        processButton(Buttons.RIGHT_THUMB_STICK, state.rightStickClick);
        processButton(Buttons.LEFT_BUMPER, state.lb);
        processButton(Buttons.RIGHT_BUMPER, state.rb);
        processButton(Buttons.LEFT_TRIGGER, Math.abs(state.leftTrigger) >= 0.1);
        processButton(Buttons.RIGHT_TRIGGER, Math.abs(state.rightTrigger) >= 0.1);
        processButton(Buttons.DPAD_UP, state.dpadUp);
        processButton(Buttons.DPAD_DOWN, state.dpadDown);
        processButton(Buttons.DPAD_LEFT, state.dpadLeft);
        processButton(Buttons.DPAD_RIGHT, state.dpadRight);
    }

    private static void processButton(int index, boolean state)
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

        if(state)
        {
            if(!buttonStates[index])
            {
                buttonStates[index] = true;
                input.handleButtonInput(controller, index, true);
            }
        }
        else if(buttonStates[index])
        {
            buttonStates[index] = false;
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
        if(button >= 0 && button < buttonStates.length)
        {
            return buttonStates[button];
        }
        return false;
    }
}
