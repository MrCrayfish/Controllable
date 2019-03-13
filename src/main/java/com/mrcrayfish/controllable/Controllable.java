package com.mrcrayfish.controllable;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.asm.ControllablePlugin;
import com.mrcrayfish.controllable.client.ControllerEvents;
import com.mrcrayfish.controllable.client.RenderEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;

import javax.annotation.Nullable;
import java.io.File;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
//@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MOD_COMPATIBILITY, clientSideOnly = true, certificateFingerprint = "4d54165f7f65cf475bf13341569655b980a5b430")
public class Controllable extends DummyModContainer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static final String[] VALID_CONTROLLERS = { "Wireless Controller" };
    private static Controller controller;
    private boolean initialized = false;

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

    @Subscribe
    public void onPreInit(FMLPreInitializationEvent event)
    {
        if(!initialized)
        {
            try
            {
                LOGGER.info("Initializing controllers");
                Controllers.create();
            }
            catch(LWJGLException e)
            {
                e.printStackTrace();
            }

            Controllers.poll();

            LOGGER.info("Scanning for a controller...");
            int count = Controllers.getControllerCount();
            for(int i = 0; i < count; i++)
            {
                org.lwjgl.input.Controller controller = Controllers.getController(i);
                for(String validController : VALID_CONTROLLERS)
                {
                    if(validController.equals(controller.getName()))
                    {
                        Controllable.controller = new Controller(controller);
                        LOGGER.info("Found controller: " + controller.getName());
                    }
                }
            }
            initialized = true;
        }

        if(Controllable.controller != null)
        {
            LOGGER.info("Registering controller events");
            MinecraftForge.EVENT_BUS.register(new ControllerEvents());
            MinecraftForge.EVENT_BUS.register(new RenderEvents());
        }
        else
        {
            LOGGER.info("Failed to find a controller. You will need to restart the game if you plug in a controller. If you don't want a controller, it is safe to ignore this message.");
        }
    }
}
