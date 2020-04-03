package com.mrcrayfish.controllable.client;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.config.gson.GsonConfig;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.registry.ButtonRegistry;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Author: MrCrayfish
 */
public class ControllerProperties
{

    private static File controllableConfigFolder;

    private static File file;
    private static boolean loaded = false;
    private static String lastController = "";
    private static String selectedMapping = "";

    private static File buttonBindingsFile;
    private static Config<ButtonRegistry.ButtonConfigData> buttonConfig;

    public static void load(File configFolder)
    {

        if(!loaded)
        {
            controllableConfigFolder = new File(configFolder, "controllable");
            Controllable.LOGGER.info("Controllable config folder: {}", controllableConfigFolder.getAbsolutePath());
            Properties properties = new Properties();
            file = new File(controllableConfigFolder, "controller.properties");
            buttonBindingsFile = new File(controllableConfigFolder, "button_bindings.json");
            try
            {
                if(file.createNewFile())
                {
                    Controllable.LOGGER.info("Successfully created controller properties");
                }
                if(file.exists())
                {
                    properties.load(new FileInputStream(file));
                    lastController = properties.getProperty("CurrentController", "");
                    selectedMapping = properties.getProperty("SelectedMapping", "");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            loaded = true;
        }
    }

    public static void loadActionRegistry() {
        try {
            if (buttonConfig == null) saveActionRegistry();

            Controllable.getButtonRegistry().loadFromConfig(buttonConfig);
            saveActionRegistry();
        } catch (ConfigLoadException e) {
            throw new IllegalStateException("Unable to load button bindings", e);
        }
    }

    /**
     * Constructs and saves the mapping
     *
     * If the file does not exist, it gets created and
     * saved with the default value provided in the constructor
     * @throws ConfigLoadException
     */
    public static void saveActionRegistry() throws ConfigLoadException {
        buttonConfig = Controllable.getButtonRegistry().saveMappings(ControllerProperties::instantiateConfig, buttonConfig != null);
    }


    private static Config<ButtonRegistry.ButtonConfigData> instantiateConfig(ButtonRegistry.ButtonConfigData buttonConfigData) {
        try {
            Controllable.LOGGER.info("Saving button bindings to {}", buttonBindingsFile.getAbsolutePath());
            Validate.notNull(buttonConfigData);
            return new GsonConfig<>(buttonConfigData, buttonBindingsFile);
        } catch (ConfigLoadException e) {
            throw new IllegalStateException("Unable to save button bindings", e);
        }
    }

    static void save()
    {
        if(!loaded)
            return;

        Properties properties = new Properties();
        properties.setProperty("LastController", lastController);
        properties.setProperty("SelectedMapping", selectedMapping);
        try
        {
            properties.store(new FileOutputStream(file), "Controller Properties");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String getLastController()
    {
        return lastController;
    }

    public static void setLastController(String lastController)
    {
        ControllerProperties.lastController = lastController;
    }

    public static String getSelectedMapping()
    {
        return selectedMapping;
    }

    public static void setSelectedMapping(String selectedMapping)
    {
        ControllerProperties.selectedMapping = selectedMapping;
    }
}
