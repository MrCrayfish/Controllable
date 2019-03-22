package com.mrcrayfish.controllable.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mrcrayfish.controllable.Controllable;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * Author: MrCrayfish
 */
public class Mappings
{
    private static final Map<String, Entry> MAPPINGS = new HashMap<>();

    private static boolean loaded = false;

    static
    {
        MAPPINGS.put("Default", null);
    }

    public static void load(File configFolder)
    {
        if(loaded)
            return;

        loadInternalMapping("defender_game_racer_x7");

        File folder = new File(configFolder, "controllable/mappings");
        if(!folder.mkdirs())
        {
            Controllable.LOGGER.error("Failed to created Controllable config folder");
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

        loadPreferences(configFolder);

        loaded = true;
    }

    private static void loadPreferences(File configFolder)
    {
        try
        {
            Properties properties = new Properties();
            File file = new File(configFolder, "controllable/controller.prefs");
            if(!file.createNewFile())
            {
                Controllable.LOGGER.error("Failed to create controller preferences");
            }
            if(file.exists())
            {
                properties.load(new FileInputStream(file));

                String currentController = properties.getProperty("CurrentController", "");
                String selectedMapping = properties.getProperty("SelectedMapping", "");

                boolean changed = false;
                Controller controller = Controllable.getController();
                if(controller != null)
                {
                    boolean sameController = controller.getName().equals(currentController);
                    if(currentController.trim().isEmpty() || !sameController)
                    {
                        properties.setProperty("CurrentController", controller.getName());
                        changed = true;
                    }

                    if(selectedMapping.trim().isEmpty() || !sameController)
                    {
                        String mapping = controller.getName();
                        Entry entry = MAPPINGS.get(mapping);
                        controller.setMapping(entry);
                        properties.setProperty("SelectedMapping", mapping);
                        changed = true;
                    }
                    else
                    {
                        Entry entry = MAPPINGS.get(selectedMapping);
                        controller.setMapping(entry);
                    }
                }

                if(changed)
                {
                    properties.store(new FileOutputStream(file), "Controller Preferences");
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void loadInternalMapping(String mappingName)
    {
        try(Reader reader = new InputStreamReader(Mappings.class.getResourceAsStream("/mappings/" + mappingName + ".json")))
        {
            loadMapping(reader).internal = true;
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

    private static Entry loadMapping(Reader reader)
    {
        JsonElement element = new JsonParser().parse(reader);
        String id = element.getAsJsonObject().get("id").getAsString();
        String name = element.getAsJsonObject().get("name").getAsString();
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
        Entry entry = new Entry(id, name, reassignments);
        MAPPINGS.put(id, entry);
        return entry;
    }

    @Nullable
    public static void updateControllerMappings(Controller controller)
    {
        controller.setMapping(MAPPINGS.get(controller.getName()));
    }

    public static class Entry
    {
        private String id;
        private String name;
        private Map<Integer, Integer> reassignments;
        private boolean internal;

        public Entry(String id, String name, Map<Integer, Integer> reassignments)
        {
            this.id = id;
            this.name = name;
            this.reassignments = reassignments;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public Map<Integer, Integer> getReassignments()
        {
            return reassignments;
        }

        public boolean isInternal()
        {
            return internal;
        }

        public int remap(int button)
        {
            Integer value = reassignments.get(button);
            if(value != null)
            {
                return value;
            }
            return button;
        }
    }
}