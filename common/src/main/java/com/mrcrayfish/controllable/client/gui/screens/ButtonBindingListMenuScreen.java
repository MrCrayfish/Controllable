package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public abstract class ButtonBindingListMenuScreen extends ListMenuScreen
{
    protected Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    protected ButtonBindingListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(parent, title, itemHeight);
        this.categories.put("key.categories.controllable_custom", new ArrayList<>());
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
    }

    @Override
    protected List<Item> constructEntries()
    {
        return this.createItems(false);
    }

    public List<Item> createItems(boolean showUnbound)
    {
        List<Item> items = new ArrayList<>();

        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Add all button bindings to the appropriate category or create a new one
        Controllable.getBindingRegistry().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding -> {
            // Only show unbound bindings for select binding screen for radial menu
            if(showUnbound && !binding.isUnbound()) return;
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            boolean isCustom = category.equals("key.categories.controllable_custom");
            if(!list.isEmpty() || isCustom)
            {
                Collections.sort(list);
                items.add(new TitleItem(Component.translatable(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                list.forEach(binding -> items.add(this.createItemFromBinding(binding)));
                if(isCustom)
                {
                    Component addKeybind = ClientHelper.join(Icons.KEY_CAP, Component.translatable("controllable.gui.add_key_bind"));
                    items.add(new OneWidgetItem(Button.builder(addKeybind, btn -> {
                        this.minecraft.setScreen(new SelectKeyBindingScreen(this, this::rebuildItems));
                    }).build()));
                }
            }
        });
        return items;
    }

    protected abstract Item createItemFromBinding(ButtonBinding binding);

    public class OneWidgetItem extends Item
    {
        private final AbstractWidget widget;

        public OneWidgetItem(AbstractWidget widget)
        {
            super(CommonComponents.EMPTY);
            this.widget = widget;
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.widget.setWidth((int) (width * 0.5));
            this.widget.setX(left + (width - this.widget.getWidth()) / 2);
            this.widget.setY(top);
            this.widget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.widget);
        }
    }
}
