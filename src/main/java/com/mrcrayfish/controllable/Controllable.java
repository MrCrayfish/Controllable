package com.mrcrayfish.controllable;

import com.google.common.eventbus.Subscribe;
import com.mrcrayfish.controllable.client.ControllerEvents;
import com.mrcrayfish.controllable.client.RenderEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MOD_COMPATIBILITY, clientSideOnly = true)
public class Controllable
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static final String[] VALID_CONTROLLERS = { "Wireless Controller" };
    private static Controller controller;
    private boolean initialized = false;

    @Nullable
    public static Controller getController()
    {
        return controller;
    }

    @Mod.EventHandler
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
                for(int j = 0; j < VALID_CONTROLLERS.length; j++)
                {
                    if(VALID_CONTROLLERS[j].equals(controller.getName()))
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
