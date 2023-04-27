package com.mrcrayfish.controllable.client.input;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.ControllerToast;
import com.mrcrayfish.controllable.client.gui.screens.ConfirmationScreen;
import com.mrcrayfish.controllable.client.gui.screens.PendingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
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
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public abstract class ControllerManager
{
    public static final String MAPPINGS_URL = "https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt";

    protected Controller activeController;
    protected Map<Number, Pair<Integer, String>> controllers = new HashMap<>();

    public abstract void init();

    public abstract void dispose();

    public abstract Controller createController(int deviceIndex, Number jid);

    @Nullable
    public abstract Controller connectToFirstGameController();

    public abstract void updateMappings(InputStream is) throws IOException;

    protected abstract int getRawControllerCount();

    protected abstract Map<Number, Pair<Integer, String>> createRawControllerMap();

    public final void tick()
    {
        if(this.getRawControllerCount() == this.controllers.size())
            return;

        Map<Number, Pair<Integer, String>> oldControllers = this.controllers;
        this.controllers = this.createRawControllerMap();

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

    protected void sendControllerToast(boolean connected, @Nullable Controller controller)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && controller != null)
        {
            mc.getToasts().addToast(new ControllerToast(connected, controller.getName()));
        }
    }

    public Map<Number, Pair<Integer, String>> getControllers()
    {
        return this.controllers;
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
        }
        else
        {
            this.activeController = null;
        }
        return true;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public final void onClientFinishedLoading()
    {
        /* Apply internal mappings */
        try(InputStream is = ControllerManager.class.getResourceAsStream("/gamecontrollerdb.txt"))
        {
            if(is != null)
            {
                Constants.LOG.info("Applying gamepad mappings from internal database");
                this.updateMappings(is);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        /* Apply local mappings */
        File mappings = new File(FMLPaths.CONFIGDIR.get().resolve("controllable").toFile(), "gamecontrollerdb.txt");
        if(mappings.exists())
        {
            Constants.LOG.info("Applying gamepad mappings from: {}", mappings);
            try(InputStream is = new BufferedInputStream(new FileInputStream(mappings)))
            {
                this.updateMappings(is);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /* Attempts to load the first game controller connected if auto select is enabled */
        if(Config.CLIENT.options.autoSelect.get())
        {
            this.connectToFirstGameController();
        }
    }

    public void downloadMappings(@Nullable Screen parentScreen)
    {
        Constants.LOG.info("Downloading mappings from: {}", ControllerManager.MAPPINGS_URL);
        File mappings = new File(FMLPaths.CONFIGDIR.get().resolve("controllable").toFile(), "gamecontrollerdb.txt");
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
                        this.updateMappings(is);
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
