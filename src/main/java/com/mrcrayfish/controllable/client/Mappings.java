package com.mrcrayfish.controllable.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mrcrayfish.controllable.Controllable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class Mappings
{
    private static final Map<String, Map<Integer, Integer>> MAPPINGS = new HashMap<>();

    private static boolean loaded = false;

    public static void load(File configFolder)
    {
        if(loaded) return;

        loadInternalMapping("ps4_controller");
        loadInternalMapping("usb_controller");

        File folder = new File(configFolder, "controllable/mappings");
        if(folder.mkdirs())
        {
            Controllable.LOGGER.info("Successfully created mappings folder in config");
        }

        File[] files = folder.listFiles();
        if(files != null)
        {
            for(File file : files)
            {
                if(file.isFile())
                {
                    loadMapping(file);
                }
            }
        }

        loaded = true;
    }

    private static void loadInternalMapping(String mappingName)
    {
        try(Reader reader = new InputStreamReader(Mappings.class.getResourceAsStream("/mappings/" + mappingName + ".json")))
        {
            loadMapping(reader);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void loadMapping(File file)
    {
        try(Reader reader = new InputStreamReader(new FileInputStream(file)))
        {
            loadMapping(reader);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void loadMapping(Reader reader)
    {
        JsonElement element = new JsonParser().parse(reader);
        String name = element.getAsJsonObject().get("id").getAsString();
        Map<Integer, Integer> reassignments = new HashMap<>();
        if(element.getAsJsonObject().has("reassign"))
        {
            JsonArray array = element.getAsJsonObject().get("reassign").getAsJsonArray();
            array.forEach(jsonElement ->
            {
                int index = jsonElement.getAsJsonObject().get("index").getAsInt();
                int with = jsonElement.getAsJsonObject().get("with").getAsInt();
                reassignments.put(index, with);
            });
        }
        MAPPINGS.put(name, reassignments);
    }

    public static int remap(Controller controller, int button)
    {
        String name = controller.getRawController().getName();
        if(MAPPINGS.containsKey(name))
        {
            Map<Integer, Integer> reassignments = MAPPINGS.get(name);
            Integer value = reassignments.get(button);
            if(value != null)
            {
                return value;
            }
        }
        return button;
    }
}