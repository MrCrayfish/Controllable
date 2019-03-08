package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.event.ControllerInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MOD_COMPATIBILITY, clientSideOnly = true)
public class Controllable
{
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
    }
}
