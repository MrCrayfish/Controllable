package com.mrcrayfish.controllable.client.binding;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class BindingRegistry
{
    static
    {
        getInstance().register(ButtonBindings.JUMP);
        getInstance().register(ButtonBindings.SNEAK);
        getInstance().register(ButtonBindings.SPRINT);
        getInstance().register(ButtonBindings.OPEN_INVENTORY);
        getInstance().register(ButtonBindings.CLOSE_INVENTORY);
        getInstance().register(ButtonBindings.SWAP_HANDS);
        getInstance().register(ButtonBindings.DROP_ITEM);
        getInstance().register(ButtonBindings.USE_ITEM);
        getInstance().register(ButtonBindings.ATTACK);
        getInstance().register(ButtonBindings.PICK_BLOCK);
        getInstance().register(ButtonBindings.PLAYER_LIST);
        getInstance().register(ButtonBindings.TOGGLE_PERSPECTIVE);
        getInstance().register(ButtonBindings.SCREENSHOT);
        getInstance().register(ButtonBindings.SCROLL_LEFT);
        getInstance().register(ButtonBindings.SCROLL_RIGHT);
        getInstance().register(ButtonBindings.PAUSE_GAME);
        getInstance().register(ButtonBindings.NEXT_CREATIVE_TAB);
        getInstance().register(ButtonBindings.PREVIOUS_CREATIVE_TAB);
        getInstance().register(ButtonBindings.NEXT_RECIPE_TAB);
        getInstance().register(ButtonBindings.PREVIOUS_RECIPE_TAB);
        getInstance().register(ButtonBindings.NAVIGATE_UP);
        getInstance().register(ButtonBindings.NAVIGATE_DOWN);
        getInstance().register(ButtonBindings.NAVIGATE_LEFT);
        getInstance().register(ButtonBindings.NAVIGATE_RIGHT);
        getInstance().register(ButtonBindings.PICKUP_ITEM);
        getInstance().register(ButtonBindings.QUICK_MOVE);
        getInstance().register(ButtonBindings.SPLIT_STACK);
        getInstance().register(ButtonBindings.SOCIAL_INTERACTIONS);
        getInstance().register(ButtonBindings.ADVANCEMENTS);
        getInstance().register(ButtonBindings.HIGHLIGHT_PLAYERS);
        getInstance().register(ButtonBindings.CINEMATIC_CAMERA);
        getInstance().register(ButtonBindings.FULLSCREEN);
        getInstance().register(ButtonBindings.DEBUG_INFO);
        getInstance().register(ButtonBindings.RADIAL_MENU);
        Stream.of(ButtonBindings.HOTBAR_SLOTS).forEach(binding -> getInstance().register(binding));
        getInstance().register(ButtonBindings.TOGGLE_CRAFT_BOOK);
        getInstance().register(ButtonBindings.OPEN_CONTROLLABLE_SETTINGS);
        getInstance().register(ButtonBindings.OPEN_CHAT);
    }

    private static BindingRegistry instance;

    public static BindingRegistry getInstance()
    {
        if(instance == null)
        {
            instance = new BindingRegistry();
        }
        return instance;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<ButtonBinding> bindings = new ArrayList<>();
    private final Map<String, ButtonBinding> registeredBindings = new HashMap<>();
    private final Map<String, KeyAdapterBinding> keyAdapters = new HashMap<>();
    private final Map<Integer, List<ButtonBinding>> idToButtonList = new HashMap<>();

    private BindingRegistry() {}

    List<ButtonBinding> getRegisteredBindings()
    {
        return this.bindings;
    }

    List<ButtonBinding> getBindingListForButton(int button)
    {
        List<ButtonBinding> list = this.idToButtonList.get(button);
        return list != null ? ImmutableList.copyOf(list) : ImmutableList.of();
    }

    @Nullable
    public ButtonBinding getBindingByDescriptionKey(String key)
    {
        return Stream.concat(this.registeredBindings.values().stream(), this.keyAdapters.values().stream()).filter(binding -> binding.getDescription().equals(key)).findFirst().orElse(null);
    }

    public List<ButtonBinding> getBindings()
    {
        return ImmutableList.copyOf(this.bindings);
    }

    public Map<String, KeyAdapterBinding> getKeyAdapters()
    {
        return this.keyAdapters;
    }

    @Nullable
    public KeyAdapterBinding getKeyAdapterByDescriptionKey(String key)
    {
        return this.keyAdapters.get(key);
    }

    public void register(ButtonBinding binding)
    {
        Preconditions.checkArgument(!(binding instanceof KeyAdapterBinding), "A key adapter binding can not be registered");
        if(this.registeredBindings.putIfAbsent(binding.getDescription(), binding) == null)
        {
            this.bindings.add(binding);
            if(binding.getButton() != -1)
            {
                this.idToButtonList.computeIfAbsent(binding.getButton(), i -> new ArrayList<>()).add(binding);
            }
        }
    }

    public void addKeyAdapter(KeyAdapterBinding binding)
    {
        if(this.keyAdapters.putIfAbsent(binding.getDescription(), binding) == null)
        {
            this.bindings.add(binding);
            if(binding.getButton() != -1)
            {
                this.idToButtonList.computeIfAbsent(binding.getButton(), i -> new ArrayList<>()).add(binding);
            }
            this.save();
        }
    }

    public void removeKeyAdapter(KeyAdapterBinding binding)
    {
        if(this.bindings.remove(binding))
        {
            this.keyAdapters.remove(binding.getDescription());
            this.idToButtonList.remove(binding.getButton());
            this.save();
        }
    }

    public void resetBindingHash()
    {
        this.idToButtonList.clear();
        this.bindings.stream().filter(binding -> binding.getButton() != -1).forEach(binding -> {
            this.idToButtonList.computeIfAbsent(binding.getButton(), i -> new ArrayList<>()).add(binding);
        });
    }

    public void load()
    {
        try
        {
            // Load regular button bindings
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("bindings.json");
            MoreFiles.createParentDirectories(path);
            if(Files.exists(path))
            {
                try(BufferedReader reader = Files.newBufferedReader(path))
                {
                    JsonObject adapters = GSON.fromJson(reader, JsonObject.class);
                    this.registeredBindings.values().stream().filter(ButtonBinding::isNotReserved).forEach(binding -> {
                        String description = binding.getDescription();
                        if(adapters.get(description) instanceof JsonPrimitive value && value.isString())
                        {
                            binding.setButton(Buttons.getButtonFromName(value.getAsString()));
                        }
                    });
                }
            }
            else
            {
                Constants.LOG.info("Skipped loading bindings.properties since it doesn't exist");
            }
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to load bindings.properties", e);
        }

        try
        {
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("key_adapters.json");
            MoreFiles.createParentDirectories(path);
            if(Files.exists(path))
            {
                try(BufferedReader reader = Files.newBufferedReader(path))
                {
                    Map<String, KeyMapping> bindings = new HashMap<>();
                    for(KeyMapping mapping : Minecraft.getInstance().options.keyMappings)
                    {
                        bindings.put(mapping.getName(), mapping);
                    }
                    JsonObject adapters = GSON.fromJson(reader, JsonObject.class);
                    adapters.asMap().forEach((key, element) -> {
                        if(!(element instanceof JsonPrimitive value) || !value.isString())
                            return;
                        KeyMapping mapping = bindings.get(key);
                        if(mapping != null) {
                            int button = Buttons.getButtonFromName(StringUtils.defaultIfEmpty(element.getAsString(), ""));
                            KeyAdapterBinding keyAdapter = new KeyAdapterBinding(button, mapping);
                            if(this.keyAdapters.putIfAbsent(keyAdapter.getDescription(), keyAdapter) == null) {
                                this.bindings.add(keyAdapter);
                                if(keyAdapter.getButton() != -1) {
                                    this.idToButtonList.computeIfAbsent(keyAdapter.getButton(), i -> new ArrayList<>()).add(keyAdapter);
                                }
                            }
                        }
                    });
                }
            }
            else
            {
                Constants.LOG.info("Skipped loading key_adapters.properties since it doesn't exist");
            }
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to load key_adapters.properties", e);
        }

        this.resetBindingHash();
    }

    public void save()
    {
        try
        {
            JsonObject bindings = new JsonObject();
            this.registeredBindings.values().stream()
                .filter(ButtonBinding::isNotReserved)
                .sorted(Comparator.comparing(ButtonBinding::getDescription))
                .forEach(binding -> {
                    String name = StringUtils.defaultIfEmpty(Buttons.getNameForButton(binding.getButton()), "");
                    bindings.addProperty(binding.getDescription(), name);
                });
            String json = GSON.toJson(bindings);
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("bindings.json");
            MoreFiles.createParentDirectories(path);
            Files.writeString(path, json);
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to save bindings.json", e);
        }

        try
        {
            JsonObject adapters = new JsonObject();
            this.keyAdapters.values().stream()
                .filter(ButtonBinding::isNotReserved)
                .sorted(Comparator.comparing(ButtonBinding::getDescription))
                .forEach(binding -> {
                    String name = StringUtils.defaultIfEmpty(Buttons.getNameForButton(binding.getButton()), "");
                    adapters.addProperty(binding.getKeyMapping().getName(), name);
                });
            String json = GSON.toJson(adapters);
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("key_adapters.json");
            MoreFiles.createParentDirectories(path);
            Files.writeString(path, json);
        }
        catch(IOException e)
        {
            Constants.LOG.error("Failed to save key_adapters.json", e);
        }
    }
}
