package com.mrcrayfish.controllable.client.input;

import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.gui.toasts.ConnectionToast;
import com.mrcrayfish.controllable.client.gui.screens.ConfirmationScreen;
import com.mrcrayfish.controllable.client.gui.screens.PendingScreen;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
@ApiStatus.Internal
public abstract class AdaptiveControllerManager
{
    public static final String MAPPINGS_URL = "https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected Controller activeController;
    protected Map<Number, Pair<Integer, String>> controllers = new HashMap<>();
    protected List<DeviceInfo> lastDevices = new ArrayList<>();
    protected boolean ready = false;

    public abstract void init();

    public abstract void dispose();

    public abstract Controller createController(int deviceIndex, Number jid);

    @Nullable
    public abstract Controller connectToBestGameController();

    public abstract void updateMappings(InputStream is) throws IOException;

    protected abstract int getRawControllerCount();

    protected abstract Map<Number, Pair<Integer, String>> createRawControllerMap();

    public void tick()
    {
        this.updateControllers();
    }

    private void updateControllers()
    {
        if(this.getRawControllerCount() == this.controllers.size())
            return;

        Map<Number, Pair<Integer, String>> oldControllers = this.controllers;
        this.controllers = this.createRawControllerMap();

        // Removes all connected from the old map of connected controllers
        oldControllers.keySet().removeIf(this.controllers::containsKey);

        Controller activeController = this.getActiveController();
        if(activeController instanceof MultiController multi)
        {
            // If current controller is a multi, remove controllers that no longer exist
            for(Controller childController : multi.getControllers())
            {
                if(oldControllers.containsKey(childController.getJid()))
                {
                    this.removeActiveController(childController);
                }
            }
        }
        else if(activeController != null && oldControllers.containsKey(activeController.getJid()))
        {
            this.sendControllerToast(false, activeController);
            this.setActiveController(null);
            activeController = null;
        }

        // If no controller is active and auto select is enabled, connect to the first controller
        if(this.ready && activeController == null && Config.CLIENT.options.autoSelect.get())
        {
            activeController = this.connectToBestGameController();
            this.sendControllerToast(true, activeController);
        }
    }

    protected void sendControllerToast(boolean connected, @Nullable Controller controller)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && controller != null)
        {
            mc.getToasts().addToast(new ConnectionToast(connected, controller.getName()));
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

    public final boolean setActiveController(@Nullable Controller controller)
    {
        if(this.activeController != null)
        {
            this.activeController.close();
            this.activeController = null;
        }
        if(controller != null)
        {
            if(controller.open())
            {
                this.activeController = controller;
            }
        }
        return true;
    }

    public final boolean addActiveController(Controller controller)
    {
        if(!controller.open())
            return false;

        if(this.activeController != null)
        {
            if(this.activeController instanceof MultiController activeMultiController)
            {
                List<Controller> newControllers = new ArrayList<>(activeMultiController.getControllers());
                newControllers.add(controller);
                this.setActiveController(new MultiController(newControllers));
            }
            else
            {
                this.setActiveController(new MultiController(List.of(this.activeController, controller)));
            }
        }
        else
        {
            this.setActiveController(controller);
        }
        return true;
    }

    public final boolean removeActiveController(Controller controller)
    {
        if(this.activeController != null)
        {
            if(this.activeController instanceof MultiController m)
            {
                List<Controller> controllers = new ArrayList<>(m.getControllers());
                controllers.remove(controller);
                if(controllers.isEmpty())
                {
                    this.setActiveController(null);
                }
                else if(controllers.size() == 1)
                {
                    this.setActiveController(controllers.getFirst());
                }
                else
                {
                    this.setActiveController(new MultiController(controllers));
                }
                return true;
            }
            else if(this.activeController.getJid().equals(controller.getJid()))
            {
                this.setActiveController(null);
                return true;
            }
        }
        return false;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public final void completeSetup()
    {
        this.loadLastDevices();

        /* Apply internal mappings */
        try(InputStream is = AdaptiveControllerManager.class.getResourceAsStream("/gamecontrollerdb.txt"))
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
        try
        {
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("gamecontrollerdb.txt");
            MoreFiles.createParentDirectories(path);
            if(Files.exists(path))
            {
                Constants.LOG.info("Applying gamepad mappings from: {}", path.toAbsolutePath());
                try(InputStream is = Files.newInputStream(path))
                {
                    this.updateMappings(is);
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        /* Attempts to load the first game controller connected if auto select is enabled */
        if(Config.CLIENT.options.autoSelect.get())
        {
            this.connectToBestGameController();
        }
        this.ready = true;
    }

    public List<DeviceInfo> getLastDevices()
    {
        return this.lastDevices;
    }

    public void updateLastDevices()
    {
        this.lastDevices.clear();
        if(this.activeController != null)
        {
            if(this.activeController instanceof MultiController m)
            {
                m.getControllers().forEach(controller -> this.lastDevices.add(controller.getInfo()));
            }
            else
            {
                this.lastDevices.add(this.activeController.getInfo());
            }
        }
        this.saveLastDevices();
    }
    
    private void loadLastDevices()
    {
        try
        {
            this.lastDevices.clear();

            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("selected_controllers.json");
            MoreFiles.createParentDirectories(path);
            if(!Files.exists(path))
                return;

            try(BufferedReader reader = Files.newBufferedReader(path))
            {
                JsonObject object = GSON.fromJson(reader, JsonObject.class);
                if(!(object.get("selected") instanceof JsonArray array))
                    return;

                array.forEach(element -> {
                    if(element instanceof JsonObject child) {
                        this.lastDevices.add(DeviceInfo.fromJson(child));
                    }
                });
            }
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to load controller.properties", e);
        }
    }
    
    private void saveLastDevices()
    {
        try
        {
            // Build json object
            JsonObject object = new JsonObject();
            object.addProperty("__comment", "Information to restore the selected controllers for next load of the game");
            JsonArray selected = new JsonArray();
            this.lastDevices.forEach(info -> {
                selected.add(info.toJson());
            });
            object.add("selected", selected);
            
            // Write to file
            String json = GSON.toJson(object);
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("selected_controllers.json");
            MoreFiles.createParentDirectories(path);
            Files.writeString(path, json);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void downloadMappings(@Nullable Screen parentScreen)
    {
        Constants.LOG.info("Downloading mappings from: {}", AdaptiveControllerManager.MAPPINGS_URL);

        try
        {
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("gamecontrollerdb.txt");
            MoreFiles.createParentDirectories(path);
            CompletableFuture.supplyAsync(() -> {
                Minecraft mc = Minecraft.getInstance();
                mc.executeBlocking(() -> mc.setScreen(new PendingScreen(Component.translatable("controllable.gui.downloading_mappings"))));

                // Artificial delay to improve user experience.
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e){
                    throw new RuntimeException(e);
                }

                // Download mappings from URL
                try(InputStream in = new BufferedInputStream(new URI(MAPPINGS_URL).toURL().openStream())) {
                    try(OutputStream fos = Files.newOutputStream(path)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while((length = in.read(buffer, 0, buffer.length)) != -1) {
                            fos.write(buffer, 0, length);
                        }
                        Constants.LOG.info("Finished downloading mappings");
                        return true;
                    }
                } catch(IOException e) {
                    Constants.LOG.error("Failed to download mappings", e);
                }
                catch(URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }).thenAcceptAsync(success -> {
                if(success) {
                    Constants.LOG.info("Updating mappings...");
                    Minecraft mc = Minecraft.getInstance();
                    mc.executeBlocking(() -> {
                        try(InputStream is = Files.newInputStream(path)) {
                            this.updateMappings(is);
                            ConfirmationScreen infoScreen = new ConfirmationScreen(parentScreen, Component.translatable("controllable.gui.mappings_updated"), result -> true);
                            infoScreen.setPositiveText(CommonComponents.GUI_BACK);
                            infoScreen.setNegativeText(null);
                            infoScreen.setIcon(ConfirmationScreen.Icon.INFO);
                            mc.setScreen(infoScreen);
                        } catch(IOException e) {
                            Constants.LOG.error("Failed to update mappings", e);
                        }
                    });
                }
            });
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
