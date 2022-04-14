package com.mrcrayfish.controllable.client;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
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
        getInstance().register(ButtonBindings.INVENTORY);
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
        getInstance().register(ButtonBindings.ADVANCEMENTS);
        getInstance().register(ButtonBindings.HIGHLIGHT_PLAYERS);
        getInstance().register(ButtonBindings.CINEMATIC_CAMERA);
        getInstance().register(ButtonBindings.FULLSCREEN);
        getInstance().register(ButtonBindings.DEBUG_INFO);
        getInstance().register(ButtonBindings.RADIAL_MENU);
        Stream.of(ButtonBindings.HOTBAR_SLOTS).forEach(binding -> {
            getInstance().register(binding);
        });
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

    private List<ButtonBinding> bindings = new ArrayList<>();
    private Map<String, ButtonBinding> registeredBindings = new HashMap<>();
    private Map<String, KeyAdapterBinding> keyAdapters = new HashMap<>();
    private Map<Integer, List<ButtonBinding>> idToButtonList = new HashMap<>();

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
        // Load regular button bindings
        try(BufferedReader reader = Files.newReader(new File(Controllable.getConfigFolder(), "controllable/bindings.properties"), Charsets.UTF_8))
        {
            Properties properties = new Properties();
            properties.load(reader);
            this.registeredBindings.values().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
            {
                String name = properties.getProperty(binding.getDescription(), Buttons.getNameForButton(binding.getButton()));
                if(name != null)
                {
                    binding.setButton(Buttons.getButtonFromName(name));
                }
            });
        }
        catch(FileNotFoundException e)
        {
            Controllable.LOGGER.info("Skipped loading bindings.properties since it doesn't exist");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // Load key adapters
        try(BufferedReader reader = Files.newReader(new File(Controllable.getConfigFolder(), "controllable/key_adapters.properties"), Charsets.UTF_8))
        {
            Map<String, KeyMapping> bindings = Arrays.stream(Minecraft.getInstance().options.keyMappings).collect(Collectors.toMap(KeyMapping::getName, v -> v, (t1, t2) -> t2));
            Properties properties = new Properties();
            properties.load(reader);
            properties.forEach((key, value) ->
            {
                KeyMapping mapping = bindings.get(key.toString());
                if(mapping != null)
                {
                    int button = Buttons.getButtonFromName(StringUtils.defaultIfEmpty(value.toString(), ""));
                    KeyAdapterBinding keyAdapter = new KeyAdapterBinding(button, mapping);
                    if(this.keyAdapters.putIfAbsent(keyAdapter.getDescription(), keyAdapter) == null)
                    {
                        this.bindings.add(keyAdapter);
                        if(keyAdapter.getButton() != -1)
                        {
                            this.idToButtonList.computeIfAbsent(keyAdapter.getButton(), i -> new ArrayList<>()).add(keyAdapter);
                        }
                    }
                }
            });
        }
        catch(FileNotFoundException e)
        {
            Controllable.LOGGER.info("Skipped loading key_adapters.properties since it doesn't exist");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        this.resetBindingHash();
    }

    public void save()
    {
        try
        {
            Properties properties = new Properties();
            this.registeredBindings.values().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
            {
                String name = StringUtils.defaultIfEmpty(Buttons.getNameForButton(binding.getButton()), "");
                properties.put(binding.getDescription(), name);
            });
            File file = new File(Controllable.getConfigFolder(), "controllable/bindings.properties");
            properties.store(new FileOutputStream(file), "Button Bindings");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            Properties properties = new Properties();
            this.keyAdapters.values().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
            {
                String name = StringUtils.defaultIfEmpty(Buttons.getNameForButton(binding.getButton()), "");
                properties.put(binding.getKeyMapping().getName(), name);
            });
            File file = new File(Controllable.getConfigFolder(), "controllable/key_adapters.properties");
            properties.store(new FileOutputStream(file), "Key Adapters");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
