package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;

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

    // TODO add async poll rate Hz setting
    // TODO move the async settings into ControllerOptions
    private static Boolean asyncPolling = null;

    public static void load(File configFolder)
    {
        if(!loaded)
        {
            Properties properties = new Properties();
            file = new File(configFolder, "controllable/controller.properties");
            try
            {
                if(file.createNewFile())
                {
                    Controllable.LOGGER.info("Successfully created controller properties");
                }
                if(file.exists())
                {
                    try(FileInputStream is = new FileInputStream(file))
                    {
                        properties.load(is);
                        lastController = properties.getProperty("CurrentController", "");
                        selectedMapping = properties.getProperty("SelectedMapping", "");
                        String asyncPollingStr = properties.getProperty("AsyncPolling");
                        asyncPolling = asyncPollingStr == null ? null : Boolean.parseBoolean(asyncPollingStr);
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

        // Only write the AsyncPolling setting to file if it is set; this way we can change the default after it's
        // determined to be stable, and existing users who upgrade won't be stuck at the old setting.
        if(asyncPolling != null)
        {
            properties.setProperty("AsyncPolling", asyncPolling.toString());
        }

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

    public static boolean isAsyncPollingEnabled()
    {
        // (default off if null)
        return Boolean.TRUE.equals(asyncPolling);
    }
}
