package com.mrcrayfish.controllable.client;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.gui.screens.ConfirmationScreen;
import com.mrcrayfish.controllable.client.gui.screens.PendingScreen;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.controllable.platform.Services;
import com.sun.jna.Memory;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
public class ControllerManager
{
    public static final String MAPPINGS_URL = "https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt";

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

        if(controller == null && Config.CLIENT.client.options.autoSelect.get())
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
                Constants.LOG.info("Applying gamepad mappings from internal database");
                ControllerManager.updateMappings(is);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        /* Apply local mappings */
        File mappings = new File(Services.PLATFORM.getConfigPath().resolve("controllable").toFile(), "gamecontrollerdb.txt");
        if(mappings.exists())
        {
            Constants.LOG.info("Applying gamepad mappings from: {}", mappings);
            try(InputStream is = new BufferedInputStream(new FileInputStream(mappings)))
            {
                ControllerManager.updateMappings(is);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /* Attempts to load the first game controller connected if auto select is enabled */
        if(Config.CLIENT.client.options.autoSelect.get())
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


    public static void updateMappings(InputStream is) throws IOException
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

    public static void downloadMappings(@Nullable Screen parentScreen)
    {
        Constants.LOG.info("Downloading mappings from: {}", MAPPINGS_URL);
        File mappings = new File(Services.PLATFORM.getConfigPath().resolve("controllable").toFile(), "gamecontrollerdb.txt");
        CompletableFuture.supplyAsync(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.executeBlocking(() -> mc.setScreen(new PendingScreen(Component.translatable("controllable.gui.downloading_mappings"))));

            // Artificial delay to improve user experience.
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            try(InputStream in = new BufferedInputStream(new URL(MAPPINGS_URL).openStream()))
            {
                try(FileOutputStream fos = new FileOutputStream(mappings))
                {
                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = in.read(buffer, 0, buffer.length)) != -1)
                    {
                        fos.write(buffer, 0, length);
                    }
                    Constants.LOG.info("Finished downloading mappings");
                    return true;
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return false;
        }).thenAcceptAsync(success -> {
            if(success)
            {
                Constants.LOG.info("Updating mappings...");
                Minecraft mc = Minecraft.getInstance();
                mc.executeBlocking(() ->
                {
                    try(InputStream is = new BufferedInputStream(new FileInputStream(mappings)))
                    {
                        updateMappings(is);
                        ConfirmationScreen infoScreen = new ConfirmationScreen(parentScreen, Component.translatable("controllable.gui.mappings_updated"), result -> true);
                        infoScreen.setPositiveText(CommonComponents.GUI_BACK);
                        infoScreen.setNegativeText(null);
                        infoScreen.setIcon(ConfirmationScreen.Icon.INFO);
                        mc.setScreen(infoScreen);
                    }
                    catch(IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
}
