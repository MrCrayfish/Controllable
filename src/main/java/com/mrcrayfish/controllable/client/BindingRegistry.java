package com.mrcrayfish.controllable.client;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.mrcrayfish.controllable.Controllable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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

    public List<ButtonBinding> getBindings()
    {
        return ImmutableList.copyOf(this.bindings);
    }

    public void register(ButtonBinding binding)
    {
        if(this.registeredBindings.putIfAbsent(binding.getDescription(), binding) == null)
        {
            this.bindings.add(binding);
            this.idToButtonList.computeIfAbsent(binding.getButton(), i -> new ArrayList<>()).add(binding);
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
        File file = new File(Controllable.getConfigFolder(), "controllable/bindings.properties");
        try(BufferedReader reader = Files.newReader(file, Charsets.UTF_8))
        {
            Properties properties = new Properties();
            properties.load(reader);
            this.bindings.forEach(binding ->
            {
                String name = properties.getProperty(binding.getDescription(), Buttons.getNameForButton(binding.getButton()));
                if(name != null)
                {
                    binding.setButton(Buttons.getButtonFromName(name));
                }
            });
            this.resetBindingHash();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void save()
    {
        Properties properties = new Properties();
        this.bindings.forEach(binding ->
        {
            String name = Buttons.getNameForButton(binding.getButton());
            if(name != null)
            {
                properties.put(binding.getDescription(), name);
            }
        });

        try
        {
            File file = new File(Controllable.getConfigFolder(), "controllable/bindings.properties");
            properties.store(new FileOutputStream(file), "Button Bindings");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
