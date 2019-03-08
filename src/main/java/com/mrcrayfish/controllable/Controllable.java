package com.mrcrayfish.controllable;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.client.Events;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class Controllable extends DummyModContainer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static final String[] VALID_CONTROLLERS = { "Wireless Controller" };
    private static Controller controller;
    private boolean initialized = false;

    public Controllable()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = Reference.MOD_ID;
        meta.name = Reference.MOD_NAME;
        meta.version = Reference.MOD_VERSION;
        meta.description = "Adds in the ability to use a controller to play Minecraft";
        meta.version = Reference.MOD_VERSION;
        meta.authorList = Lists.newArrayList("MrCrayfish");
        meta.url = "https://mrcrayfish.com/mods?id=controllable";
        meta.updateJSON = "https://raw.githubusercontent.com/MrCrayfish/Controllable/master/update.json";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event)
    {
        if(event.getSide() == Side.CLIENT)
        {
            if(!initialized)
            {
                try
                {
                    Controllers.create();
                }
                catch(LWJGLException e)
                {
                    e.printStackTrace();
                }

                Controllers.poll();

                int count = Controllers.getControllerCount();
                for(int i = 0; i < count; i++)
                {
                    Controller controller = Controllers.getController(i);
                    for(int j = 0; j < VALID_CONTROLLERS.length; j++)
                    {
                        if(VALID_CONTROLLERS[j].equals(controller.getName()))
                        {
                            Controllable.controller = controller;
                        }
                    }
                }
                initialized = true;
            }

            MinecraftForge.EVENT_BUS.register(new Events());
        }
    }
}
