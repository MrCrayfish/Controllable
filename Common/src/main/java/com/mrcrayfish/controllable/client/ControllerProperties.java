package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;

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
    private static File file;
    private static boolean loaded = false;
    private static String lastController = "";
    private static String selectedMapping = "";

    public static void load(File configFolder)
    {
        if(!loaded)
        {
            Properties properties = new Properties();
            File folder = new File(configFolder, "controllable");
            folder.mkdirs();
            file = new File(folder, "controller.properties");
            try
            {
                if(file.createNewFile())
                {
                    Constants.LOG.info("Successfully created controller properties");
                }
                if(file.exists())
                {
                    try(FileInputStream is = new FileInputStream(file))
                    {
                        properties.load(is);
                        lastController = properties.getProperty("CurrentController", "");
                        selectedMapping = properties.getProperty("SelectedMapping", "");
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            loaded = true;
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
