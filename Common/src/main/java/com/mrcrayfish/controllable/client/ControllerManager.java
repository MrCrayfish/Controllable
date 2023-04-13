package com.mrcrayfish.controllable.client;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.sun.jna.Memory;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.*;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.SDL_NumJoysticks;
import static io.github.libsdl4j.api.rwops.SdlRWops.SDL_RWFromConstMem;

/**
 * Author: MrCrayfish
 */
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
    private Map<Integer, String> controllers = new HashMap<>();

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

    public void setActiveController(Controller controller)
    {
        if(this.activeController != null)
        {
            this.activeController.dispose();
        }
        if(controller != null)
        {
            this.activeController = controller;
            Mappings.updateControllerMappings(controller);
        }
        else
        {
            this.activeController = null;
        }
    }

    public Map<Integer, String> getControllers()
    {
        return this.controllers;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public void tick()
    {
        int connectedCount = 0;
        int joysticksCount = SDL_NumJoysticks();
        for(int jid = 0; jid <= joysticksCount; jid++)
        {
            if(SDL_IsGameController(jid))
            {
                connectedCount++;
            }
        }

        if(connectedCount == this.controllers.size())
            return;

        Map<Integer, String> oldControllers = this.controllers;
        Map<Integer, String> newControllers = new HashMap<>();
        for(int jid = 0; jid <= joysticksCount; jid++)
        {
            if(SDL_IsGameController(jid))
            {
                String controllerName = SDL_GameControllerNameForIndex(jid);
                newControllers.put(jid, controllerName);
            }
        }

        this.controllers = newControllers;

        newControllers.forEach((jid, name) ->
        {
            if(!oldControllers.containsKey(jid))
            {
                Minecraft.getInstance().doRunTask(() ->
                {
                    Controller controller = this.getActiveController();
                    if(controller != null)
                        return;

                    if(Config.CLIENT.client.options.autoSelect.get())
                    {
                        this.setActiveController(controller = new Controller(jid));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null && controller != null)
                    {
                        mc.getToasts().addToast(new ControllerToast(true, controller.getName()));
                    }
                });
            }
        });

        oldControllers.forEach((jid, name) ->
        {
            if(!newControllers.containsKey(jid))
            {
                Minecraft.getInstance().doRunTask(() ->
                {
                    Controller controller = this.getActiveController();
                    if(controller == null || controller.getJid() != jid)
                        return;

                    this.setActiveController(null);

                    if(Config.CLIENT.client.options.autoSelect.get() && this.getControllerCount() > 0)
                    {
                        Optional<Integer> optional = this.getControllers().keySet().stream().min(Comparator.comparing(i -> i));
                        optional.ifPresent(minJid -> this.setActiveController(new Controller(minJid)));
                    }

                    Minecraft mc = Minecraft.getInstance();
                    if(mc.player != null)
                    {
                        Minecraft.getInstance().getToasts().addToast(new ControllerToast(false, controller.getName()));
                    }
                });
            }
        });
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

        /* Attempts to load the first controller connected if auto select is enabled */
        if(Config.CLIENT.client.options.autoSelect.get())
        {
            if(SDL_IsGameController(0))
            {
                this.setActiveController(new Controller(0));
            }
        }
    }
}
