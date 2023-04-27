package com.mrcrayfish.controllable.client.input.sdl2;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.sun.jna.Memory;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_GAMECONTROLLER;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_JOYSTICK;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.SDL_JoystickGetDeviceInstanceID;
import static io.github.libsdl4j.api.joystick.SdlJoystick.SDL_NumJoysticks;
import static io.github.libsdl4j.api.rwops.SdlRWops.SDL_RWFromConstMem;

/**
 * Author: MrCrayfish
 */
public class SDL2ControllerManager extends ControllerManager
{
    @Override
    public void init()
    {
        SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_GAMECONTROLLER);
    }

    @Override
    public void dispose()
    {
        SDL_Quit();
    }

    @Override
    public SDL2Controller createController(int deviceIndex, Number jid)
    {
        return new SDL2Controller(deviceIndex);
    }

    @Override
    protected int getRawControllerCount()
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
        return controllerCount;
    }

    @Override
    protected Map<Number, Pair<Integer, String>> createRawControllerMap()
    {
        Map<Number, Pair<Integer, String>> controllers = new HashMap<>();
        int joysticksCount = SDL_NumJoysticks();
        for(int deviceIndex = 0; deviceIndex < joysticksCount; deviceIndex++)
        {
            if(SDL_IsGameController(deviceIndex))
            {
                SDL_JoystickID jid = SDL_JoystickGetDeviceInstanceID(deviceIndex);
                String controllerName = SDL_GameControllerNameForIndex(deviceIndex);
                controllers.put(jid, Pair.of(deviceIndex, controllerName));
            }
        }
        return controllers;
    }

    @Override
    @Nullable
    public SDL2Controller connectToFirstGameController()
    {
        int joysticksCount = SDL_NumJoysticks();
        for(int deviceIndex = 0; deviceIndex < joysticksCount; deviceIndex++)
        {
            if(SDL_IsGameController(deviceIndex))
            {
                SDL2Controller controller = new SDL2Controller(deviceIndex);
                if(this.setActiveController(controller))
                {
                    return controller;
                }
            }
        }
        return null;
    }

    @Override
    public void updateMappings(InputStream is) throws IOException
    {
        byte[] bytes = ByteStreams.toByteArray(is);
        try(Memory memory = new Memory(bytes.length))
        {
            memory.write(0, bytes, 0, bytes.length);
            SDL_RWops wops = SDL_RWFromConstMem(memory, (int) memory.size());
            int count = SDL_GameControllerAddMappingsFromRW(wops, 1);
            if(count > 0)
            {
                Constants.LOG.info("Successfully updated {} gamepad mappings", count);
                return;
            }
        }
        Constants.LOG.info("No gamepad mappings were updated");
    }
}
