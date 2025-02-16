package com.mrcrayfish.controllable.client.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class BindingRegistry
{
    private static BindingRegistry instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<ButtonBinding> bindings = new ArrayList<>();
    private final Map<String, ButtonBinding> registeredBindings = new HashMap<>();
    private final Map<String, KeyAdapterBinding> keyAdapters = new HashMap<>();
    private final Multimap<Integer, ButtonBinding> idToButtonList = TreeMultimap.create(Ordering.natural(),
        Comparator.<ButtonBinding, Integer>comparing(binding -> binding.getContext().priority()).reversed().thenComparing(ButtonBinding::compareTo)
    );

    public BindingRegistry()
    {
        Preconditions.checkState(instance == null, "Only one instance of BindingRegistry is allowed");
        this.registerBuiltinBindings();
        instance = this;
    }

    private void registerBuiltinBindings()
    {
        this.register(ButtonBindings.JUMP);
        this.register(ButtonBindings.SNEAK);
        this.register(ButtonBindings.SPRINT);
        this.register(ButtonBindings.OPEN_INVENTORY);
        this.register(ButtonBindings.CLOSE_INVENTORY);
        this.register(ButtonBindings.SWAP_HANDS);
        this.register(ButtonBindings.DROP_ITEM);
        this.register(ButtonBindings.USE_ITEM);
        this.register(ButtonBindings.ATTACK);
        this.register(ButtonBindings.PICK_BLOCK);
        this.register(ButtonBindings.PLAYER_LIST);
        this.register(ButtonBindings.TOGGLE_PERSPECTIVE);
        this.register(ButtonBindings.SCREENSHOT);
        this.register(ButtonBindings.SCROLL_HOTBAR_LEFT);
        this.register(ButtonBindings.SCROLL_HOTBAR_RIGHT);
        this.register(ButtonBindings.PAUSE_GAME);
        this.register(ButtonBindings.UNPAUSE_GAME);
        this.register(ButtonBindings.NEXT_CREATIVE_TAB);
        this.register(ButtonBindings.PREVIOUS_CREATIVE_TAB);
        this.register(ButtonBindings.NEXT_RECIPE_TAB);
        this.register(ButtonBindings.PREVIOUS_RECIPE_TAB);
        this.register(ButtonBindings.NAVIGATE_UP);
        this.register(ButtonBindings.NAVIGATE_DOWN);
        this.register(ButtonBindings.NAVIGATE_LEFT);
        this.register(ButtonBindings.NAVIGATE_RIGHT);
        this.register(ButtonBindings.PICKUP_ITEM);
        this.register(ButtonBindings.QUICK_MOVE);
        this.register(ButtonBindings.SPLIT_STACK);
        this.register(ButtonBindings.SOCIAL_INTERACTIONS);
        this.register(ButtonBindings.ADVANCEMENTS);
        this.register(ButtonBindings.HIGHLIGHT_PLAYERS);
        this.register(ButtonBindings.CINEMATIC_CAMERA);
        this.register(ButtonBindings.FULLSCREEN);
        this.register(ButtonBindings.DEBUG_INFO);
        this.register(ButtonBindings.RADIAL_MENU);
        this.register(ButtonBindings.HOTBAR_SLOT_1);
        this.register(ButtonBindings.HOTBAR_SLOT_2);
        this.register(ButtonBindings.HOTBAR_SLOT_3);
        this.register(ButtonBindings.HOTBAR_SLOT_4);
        this.register(ButtonBindings.HOTBAR_SLOT_5);
        this.register(ButtonBindings.HOTBAR_SLOT_6);
        this.register(ButtonBindings.HOTBAR_SLOT_7);
        this.register(ButtonBindings.HOTBAR_SLOT_8);
        this.register(ButtonBindings.HOTBAR_SLOT_9);
        this.register(ButtonBindings.TOGGLE_CRAFT_BOOK);
        this.register(ButtonBindings.OPEN_CONTROLLABLE_SETTINGS);
        this.register(ButtonBindings.OPEN_CHAT);
        this.register(ButtonBindings.MOVE_CURSOR_UP);
        this.register(ButtonBindings.MOVE_CURSOR_DOWN);
        this.register(ButtonBindings.MOVE_CURSOR_LEFT);
        this.register(ButtonBindings.MOVE_CURSOR_RIGHT);
        this.register(ButtonBindings.SCROLL_UP);
        this.register(ButtonBindings.SCROLL_DOWN);
        this.register(ButtonBindings.WALK_FORWARDS);
        this.register(ButtonBindings.WALK_BACKWARDS);
        this.register(ButtonBindings.STRAFE_LEFT);
        this.register(ButtonBindings.STRAFE_RIGHT);
        this.register(ButtonBindings.LOOK_UP);
        this.register(ButtonBindings.LOOK_DOWN);
        this.register(ButtonBindings.LOOK_LEFT);
        this.register(ButtonBindings.LOOK_RIGHT);
    }

    public List<ButtonBinding> getRegisteredBindings()
    {
        return this.bindings;
    }

    public Collection<ButtonBinding> getBindingsForButton(int button)
    {
        return this.idToButtonList.get(button);
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
            if(!binding.isUnbound())
            {
                this.idToButtonList.put(binding.getButton(), binding);
            }
        }
    }

    public void addKeyAdapter(KeyAdapterBinding binding)
    {
        if(this.keyAdapters.putIfAbsent(binding.getDescription(), binding) == null)
        {
            this.bindings.add(binding);
            if(!binding.isUnbound())
            {
                this.idToButtonList.put(binding.getButton(), binding);
            }
            this.save();
        }
    }

    public void removeKeyAdapter(KeyAdapterBinding binding)
    {
        if(this.bindings.remove(binding))
        {
            this.keyAdapters.remove(binding.getDescription());
            this.idToButtonList.remove(binding.getButton(), binding);
            this.save();
        }
    }

    public void rebuildCache()
    {
        Controllable.getInputHandler().clearActiveHandlers();
        this.idToButtonList.clear();
        this.bindings.stream().filter(binding -> !binding.isUnbound()).forEach(binding -> {
            this.idToButtonList.put(binding.getButton(), binding);
        });
    }

    public void completeSetup()
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
                    this.registeredBindings.values().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
                    {
                        String description = binding.getDescription();
                        if(adapters.get(description) instanceof JsonPrimitive value && value.isString())
                        {
                            ButtonBinding.setButton(binding, Buttons.getButtonFromName(value.getAsString()));
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
                                if(!keyAdapter.isUnbound()) {
                                    this.idToButtonList.put(keyAdapter.getButton(), keyAdapter);
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

        this.rebuildCache();
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

    // Patches old Iron Jetpacks versions
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static BindingRegistry getInstance()
    {
        return Controllable.getBindingRegistry();
    }
}
