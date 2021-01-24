package com.mrcrayfish.controllable.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class Mappings
{
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Entry.class, new Entry.Serializer()).create();

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
        if(folder.mkdirs())
        {
            Controllable.LOGGER.info("Successfully created Controllable config folder");
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

        initProperties();

        loaded = true;
    }

    private static void initProperties()
    {
        boolean changed = false;
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            String lastController = ControllerProperties.getLastController();
            boolean sameController = controller.getName().equals(lastController);
            if(lastController.trim().isEmpty() || !sameController)
            {
                ControllerProperties.setLastController(controller.getName());
                changed = true;
            }

            String selectedMapping = ControllerProperties.getSelectedMapping();
            if(selectedMapping.trim().isEmpty() || !sameController)
            {
                String mapping = controller.getName();
                Entry entry = MAPPINGS.get(mapping);
                controller.setMapping(entry);
                if(entry != null)
                ControllerProperties.setSelectedMapping(mapping);
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
            ControllerProperties.save();
        }
    }

    private static void loadInternalMapping(String mappingName)
    {
        try(Reader reader = new InputStreamReader(Mappings.class.getResourceAsStream("/mappings/" + mappingName + ".json")))
        {
            Entry entry = GSON.fromJson(reader, Entry.class);
            entry.internal = true;
            MAPPINGS.put(entry.getId(), entry);
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
            Entry entry = GSON.fromJson(reader, Entry.class);
            MAPPINGS.put(entry.getId(), entry);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void updateControllerMappings(Controller controller)
    {
        controller.setMapping(MAPPINGS.get(controller.getName()));
    }

    public static class Entry
    {
        private String id;
        private String name;
        private Map<Integer, Integer> reassignments;
        private boolean switchThumbsticks;
        private boolean internal;

        private Entry() {}

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
            return this.reassignments;
        }

        public boolean isThumbsticksSwitched()
        {
            return switchThumbsticks;
        }

        public void setSwitchThumbsticks(boolean switchThumbsticks)
        {
            this.switchThumbsticks = switchThumbsticks;
        }

        public boolean isInternal()
        {
            return this.internal;
        }

        public int remap(int button)
        {
            Integer value = this.reassignments.get(button);
            if(value != null)
            {
                return value;
            }
            return button;
        }

        public Entry copy()
        {
            Entry entry = new Entry(this.id, this.name, new HashMap<>(this.reassignments));
            entry.switchThumbsticks = this.switchThumbsticks;
            return entry;
        }

        public void save()
        {
            try
            {
                File mappingsFolder = new File(Minecraft.getInstance().gameDir, "config/controllable/mappings");
                mappingsFolder.mkdirs();
                String name = id.replaceAll("\\s+", "_").toLowerCase(Locale.ENGLISH) + ".json";
                String json = GSON.toJson(this);
                FileOutputStream fos = new FileOutputStream(new File(mappingsFolder, name));
                fos.write(json.getBytes());
                fos.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        public static class Serializer implements JsonSerializer<Entry>, JsonDeserializer<Entry>
        {
            @Override
            public JsonElement serialize(Entry src, Type typeOfSrc, JsonSerializationContext context)
            {
                JsonObject object = new JsonObject();
                object.addProperty("id", src.id);
                object.addProperty("name", src.name);
                object.addProperty("switchThumbsticks", src.switchThumbsticks);
                JsonArray array = new JsonArray();
                src.reassignments.forEach((index, with) -> {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("index", index);
                    entry.addProperty("with", with);
                    array.add(entry);
                });
                object.add("reassign", array);
                return object;
            }

            @Override
            public Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                Entry entry = new Entry();
                JsonObject object = json.getAsJsonObject();
                entry.id = object.get("id").getAsString();
                entry.name = object.get("name").getAsString();
                if(object.has("switchThumbsticks"))
                {
                    entry.switchThumbsticks = object.get("switchThumbsticks").getAsBoolean();
                }
                Map<Integer, Integer> reassignments = new HashMap<>();
                object.getAsJsonArray("reassign").forEach(e -> {
                    JsonObject o = e.getAsJsonObject();
                    reassignments.put(o.get("index").getAsInt(), o.get("with").getAsInt());
                });
                entry.reassignments = reassignments;
                return entry;
            }
        }
    }
}