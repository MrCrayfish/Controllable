package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.client.InputProcessor;
import com.mrcrayfish.controllable.client.ScreenEvents;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.input.glfw.GLFWControllerManager;
import com.mrcrayfish.controllable.client.input.sdl2.SDL2ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkConstants;

import javax.annotation.Nullable;
import java.io.File;

@Mod(Constants.MOD_ID)
public class Controllable
{
    private static ControllerManager manager;
    private static File configFolder;
    private static boolean jeiLoaded;

    public Controllable()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ClientBootstrap.init();
            MinecraftForge.EVENT_BUS.register(new ScreenEvents());
        });
    }

    public static void init()
    {
        configFolder = FMLPaths.CONFIGDIR.get().toFile();
        jeiLoaded = ModList.get().isLoaded("jei");
        ControllerProperties.load(configFolder);
    }

    public static ControllerInput getInput()
    {
        return InputProcessor.instance().getInput();
    }

    public static File getConfigFolder()
    {
        return configFolder;
    }

    public static boolean isJeiLoaded()
    {
        return jeiLoaded;
    }

    @Nullable
    public static Controller getController()
    {
        return getManager().getActiveController();
    }

    public static ControllerManager getManager()
    {
        if(manager == null)
        {
            if(!Minecraft.ON_OSX)
            {
                manager = new SDL2ControllerManager();
            }
            else
            {
                manager = new GLFWControllerManager();
            }
        }
        return manager;
    }
}
