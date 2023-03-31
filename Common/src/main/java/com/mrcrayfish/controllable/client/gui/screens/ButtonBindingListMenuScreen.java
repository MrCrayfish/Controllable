package com.mrcrayfish.controllable.client.gui.screens;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public abstract class ButtonBindingListMenuScreen extends ListMenuScreen
{
    protected Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    protected ButtonBindingListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(parent, title, itemHeight);
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
        this.categories.put("key.categories.controllable_custom", new ArrayList<>());
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        this.updateList(entries, false); //TODO do I need the second param?
    }

    public void updateList(List<Item> entries, boolean showUnbound)
    {
        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Add all button bindings to the appropriate category or create a new one
        BindingRegistry.getInstance().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
        {
            // Only show unbound bindings for select binding screen for radial menu
            if(showUnbound && binding.getButton() != -1) return;
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                entries.add(new TitleItem(Component.translatable(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                list.forEach(binding -> entries.add(this.createItemFromBinding(binding)));
            }
        });
    }

    protected abstract Item createItemFromBinding(ButtonBinding binding);
}
