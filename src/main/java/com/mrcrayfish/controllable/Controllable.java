package com.mrcrayfish.controllable;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.asm.ControllablePlugin;
import com.mrcrayfish.controllable.client.*;
import com.studiohartman.jamepad.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
//@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MOD_COMPATIBILITY, clientSideOnly = true, certificateFingerprint = "4d54165f7f65cf475bf13341569655b980a5b430")
public class Controllable extends DummyModContainer
{
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

    public static int getSelectedControllerIndex()
    {
        return selectedControllerIndex;
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

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event)
    {
        //ControllerProperties.load(event.getModConfigurationDirectory());

        /* Loads up the controller manager and setup shutdown cleanup */
        Controllable.manager = new ControllerManager();
        Controllable.manager.initSDLGamepad();
        Controllable.currentControllerCount = manager.getNumControllers();
        Controllable.connectedControllerNames = getConnectedControllerNames();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Controllable.manager.quitSDLGamepad()));

        /* Attempts to load the first controller connected */
        ControllerIndex index = manager.getControllerIndex(0);
        if(index.isConnected())
        {
            setController(new Controller(index));
        }

        Mappings.load(event.getModConfigurationDirectory());

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

        Minecraft mc = Minecraft.getMinecraft();
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
            connectedControllerNames = getConnectedControllerNames();

            if(mc.player != null)
            {
                String controllerName = newControllers.size() > 0 ? newControllers.get(0) : "Controller";
                Minecraft.getMinecraft().getToastGui().add(new ControllerToast(connected, controllerName));
            }
            currentControllerCount = manager.getNumControllers();
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
        if(controller.getMapping() != null)
        {
            index = controller.getMapping().remap(index);
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
}
