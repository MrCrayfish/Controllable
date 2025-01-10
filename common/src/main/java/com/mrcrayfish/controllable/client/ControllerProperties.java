package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.util.Utils;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Author: MrCrayfish
 */
public final class ControllerProperties
{
    private static ControllerProperties instance;

    private String lastController = "";
    private String selectedMapping = "";

    @ApiStatus.Internal
    public ControllerProperties()
    {
        Preconditions.checkState(instance == null, "Only one instance of ControllerProperties is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void load()
    {
        try
        {
            Path path = Utils.getConfigDirectory().resolve("controllable").resolve("controller.properties");
            MoreFiles.createParentDirectories(path);
            if(Files.exists(path))
            {
                try(InputStream is = Files.newInputStream(path))
                {
                    // Should I switch to JSON with a codec?
                    Properties properties = new Properties();
                    properties.load(is);
                    lastController = properties.getProperty("CurrentController", "");
                    selectedMapping = properties.getProperty("SelectedMapping", "");
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
        Properties properties = new Properties();
        properties.setProperty("LastController", this.lastController);
        properties.setProperty("SelectedMapping", this.selectedMapping);

        try
        {
            Path path = Utils.getConfigDirectory().resolve("controllable").resolve("controller.properties");
            MoreFiles.createParentDirectories(path);
            properties.store(Files.newOutputStream(path), "Controller Properties");
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to save controller.properties", e);
        }
    }

    public String getLastController()
    {
        return this.lastController;
    }

    public void setLastController(String lastController)
    {
        this.lastController = lastController;
    }

    public String getSelectedMapping()
    {
        return this.selectedMapping;
    }

    public void setSelectedMapping(String selectedMapping)
    {
        this.selectedMapping = selectedMapping;
    }
}
