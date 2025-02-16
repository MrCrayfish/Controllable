package com.mrcrayfish.controllable.client.input.sdl2;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import com.mrcrayfish.controllable.client.input.MultiController;
import com.mrcrayfish.controllable.util.Utils;
import com.mrcrayfish.controllable_sdl.api.gamecontroller.SdlGamecontroller;
import com.mrcrayfish.controllable_sdl.jna.SdlNativeLibraryLoader;
import com.sun.jna.Memory;
import com.mrcrayfish.controllable_sdl.api.joystick.SDL_JoystickID;
import com.mrcrayfish.controllable_sdl.api.rwops.SDL_RWops;
import org.apache.commons.lang3.tuple.Pair;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mrcrayfish.controllable_sdl.api.Sdl.SDL_Init;
import static com.mrcrayfish.controllable_sdl.api.Sdl.SDL_Quit;
import static com.mrcrayfish.controllable_sdl.api.SdlSubSystemConst.SDL_INIT_GAMECONTROLLER;
import static com.mrcrayfish.controllable_sdl.api.SdlSubSystemConst.SDL_INIT_JOYSTICK;
import static com.mrcrayfish.controllable_sdl.api.gamecontroller.SdlGamecontroller.*;
import static com.mrcrayfish.controllable_sdl.api.hints.SdlHints.SDL_SetHint;
import static com.mrcrayfish.controllable_sdl.api.hints.SdlHintsConst.*;
import static com.mrcrayfish.controllable_sdl.api.joystick.SdlJoystick.SDL_JoystickGetDeviceInstanceID;
import static com.mrcrayfish.controllable_sdl.api.joystick.SdlJoystick.SDL_NumJoysticks;
import static com.mrcrayfish.controllable_sdl.api.rwops.SdlRWops.SDL_RWFromConstMem;

/**
 * Author: MrCrayfish
 */
public class SDL2ControllerManager extends AdaptiveControllerManager
{
    static
    {
        try
        {
            // Updates the extract path of natives so it's tied to the game directory
            Path natives = Utils.getGamePath().resolve("controllable_natives");
            Path sdl = natives.resolve("SDL");
            Files.createDirectories(sdl);
            SdlNativeLibraryLoader.setExtractionPath(sdl);

            // Add a readme to the natives directory for users
            Path readMeFile = natives.resolve("README.txt");
            if(!Files.exists(readMeFile))
            {
                Files.writeString(readMeFile, """
                    This directory holds the natives for Controllable, which are used to interface
                    with game controllers and read their inputs. It is safe to delete, just make sure
                    the game is closed as the natives may be loaded; preventing you from deleting them.
                    If you are developing a modpack, make sure to exclude this directory.""");
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean lastBackgroundInput;

    @Override
    public void init()
    {
        if(Config.CLIENT.options.backgroundInput.get())
        {
            SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
            this.lastBackgroundInput = true;
        }
        SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_VERTICAL_JOY_CONS, "1");
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
    public Controller connectToBestGameController()
    {
        List<DeviceInfo> lastDevices = this.getLastDevices();
        if(!lastDevices.isEmpty())
        {
            List<SDL2Controller> selectedControllers = new ArrayList<>();
            List<SDL2Controller> availableControllers = IntStream.range(0, SDL_NumJoysticks())
                .filter(SdlGamecontroller::SDL_IsGameController)
                .mapToObj(SDL2Controller::new)
                .collect(Collectors.toCollection(ArrayList::new));
            for(DeviceInfo info : lastDevices)
            {
                Iterator<SDL2Controller> it = availableControllers.iterator();
                while(it.hasNext())
                {
                    SDL2Controller controller = it.next();
                    controller.open();
                    if(controller.getInfo().equals(info))
                    {
                        selectedControllers.add(controller);
                        it.remove();
                    }
                    controller.close();
                }
            }
            selectedControllers.forEach(this::addActiveController);

            Controller controller = this.getActiveController();
            if(controller != null)
            {
                return controller;
            }
        }

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

    @Override
    public void tick()
    {
        super.tick();

        // Updates the sdl hint for background events
        if(this.lastBackgroundInput != Config.CLIENT.options.backgroundInput.get())
        {
            this.lastBackgroundInput = Config.CLIENT.options.backgroundInput.get();
            String value = this.lastBackgroundInput ? "1" : "0";
            SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, value);
        }
    }
}
