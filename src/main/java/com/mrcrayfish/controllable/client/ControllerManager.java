package com.mrcrayfish.controllable.client;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.sun.jna.Memory;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.*;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickGetDeviceInstanceID;
import static io.github.libsdl4j.api.joystick.SdlJoystick.SDL_NumJoysticks;
import static io.github.libsdl4j.api.rwops.SdlRWops.SDL_RWFromConstMem;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ControllerManager
{
    private static ControllerManager instance;

    public static ControllerManager instance()
    {
        if(instance == null)
        {
            instance = new ControllerManager();
        }
        return instance;
    }

    private Controller activeController;
    private Map<SDL_JoystickID, Pair<Integer, String>> controllers = new HashMap<>();

    private ControllerManager() {}

    public void init()
    {
        SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_GAMECONTROLLER);
    }

    public void close()
    {
        SDL_Quit();
    }

    @Nullable
    public Controller getActiveController()
    {
        return this.activeController;
    }

    public boolean setActiveController(Controller controller)
    {
        if(this.activeController != null)
        {
            this.activeController.close();
        }
        if(controller != null)
        {
            if(!controller.open())
                return false;

            this.activeController = controller;
            Mappings.updateControllerMappings(controller);
        }
        else
        {
            this.activeController = null;
        }
        return true;
    }

    public Map<SDL_JoystickID, Pair<Integer, String>> getControllers()
    {
        return this.controllers;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public void tick()
    {
        int controllerCount = 0;
        int joysticksCount = SDL_NumJoysticks();
        for(int deviceIndex = 0; deviceIndex < joysticksCount; deviceIndex++)
        {
            if(SDL_IsGameController(deviceIndex))
            {
                controllerCount++;
            }
        }

        if(controllerCount == this.controllers.size())
            return;

        Map<SDL_JoystickID, Pair<Integer, String>> oldControllers = this.controllers;
        this.controllers = new HashMap<>();
        for(int deviceIndex = 0; deviceIndex < joysticksCount; deviceIndex++)
        {
            if(SDL_IsGameController(deviceIndex))
            {
                SDL_JoystickID jid = SDL_JoystickGetDeviceInstanceID(deviceIndex);
                String controllerName = SDL_GameControllerNameForIndex(deviceIndex);
                this.controllers.put(jid, Pair.of(deviceIndex, controllerName));
            }
        }

        // Removes all connected from the old map of connected controllers
        oldControllers.keySet().removeIf(this.controllers::containsKey);

        // If no controller is active and auto select is enabled, connect to the first controller
        Controller controller = this.getActiveController();
        if(controller != null && oldControllers.containsKey(controller.getJid()))
        {
            this.sendControllerToast(false, controller);
            this.setActiveController(null);
            controller = null;
        }

        if(controller == null && Config.CLIENT.options.autoSelect.get())
        {
            controller = this.connectToFirstGameController();
            this.sendControllerToast(true, controller);
        }
    }

    private void sendControllerToast(boolean connected, @Nullable Controller controller)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && controller != null)
        {
            mc.getToasts().addToast(new ControllerToast(connected, controller.getName()));
        }
    }

    public void onClientFinishedLoading()
    {
        /* Update gamepad mappings */
        try(InputStream is = Mappings.class.getResourceAsStream("/gamecontrollerdb.txt"))
        {
            if(is != null)
            {
                byte[] bytes = ByteStreams.toByteArray(is);
                try(Memory memory = new Memory(bytes.length))
                {
                    memory.write(0, bytes, 0, bytes.length);
                    SDL_RWops wops = SDL_RWFromConstMem(memory, (int) memory.size());
                    int count = SDL_GameControllerAddMappingsFromRW(wops, 0);
                    if(count > 0)
                    {
                        Constants.LOG.info("Successfully updated {} gamepad mappings", count);
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        /* Attempts to load the first game controller connected if auto select is enabled */
        if(Config.CLIENT.options.autoSelect.get())
        {
            this.connectToFirstGameController();
        }
    }

    @Nullable
    private Controller connectToFirstGameController()
    {
        int joysticksCount = SDL_NumJoysticks();
        for(int deviceIndex = 0; deviceIndex < joysticksCount; deviceIndex++)
        {
            if(SDL_IsGameController(deviceIndex))
            {
                Controller controller = new Controller(deviceIndex);
                if(this.setActiveController(controller))
                {
                    return controller;
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event)
    {
        instance().close();
    }
}
