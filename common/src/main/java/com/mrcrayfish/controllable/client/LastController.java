package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import com.mrcrayfish.controllable.util.Utils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Author: MrCrayfish
 */
public final class LastController
{
    private static LastController instance;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private @Nullable DeviceInfo lastDevice;

    @ApiStatus.Internal
    public LastController()
    {
        Preconditions.checkState(instance == null, "Only one instance of ControllerProperties is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void load()
    {
        try
        {
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("last_controller.json");
            MoreFiles.createParentDirectories(path);
            if(Files.exists(path))
            {
                try(BufferedReader reader = Files.newBufferedReader(path))
                {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    this.lastDevice = DeviceInfo.fromJson(element);
                }
            }
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to load controller.properties", e);
        }
    }

    @ApiStatus.Internal
    public void save()
    {
        if(this.lastDevice == null)
            return;

        try
        {
            String json = GSON.toJson(this.lastDevice.toJson());
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("last_controller.json");
            MoreFiles.createParentDirectories(path);
            Files.writeString(path, json);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLastDevice(@Nullable DeviceInfo info)
    {
        this.lastDevice = info;
        this.save();
    }

    public @Nullable DeviceInfo getLastDevice()
    {
        return this.lastDevice;
    }
}
