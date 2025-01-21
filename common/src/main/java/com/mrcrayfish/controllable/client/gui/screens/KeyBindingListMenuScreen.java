package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.ISearchable;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public abstract class KeyBindingListMenuScreen extends ListMenuScreen
{
    private static final List<KeyMapping> DEFAULT_BINDINGS = Util.make(() -> {
        Options options = Minecraft.getInstance().options;
        List<KeyMapping> bindings = new ArrayList<>();
        bindings.add(options.keyAttack);
        bindings.add(options.keyUse);
        bindings.add(options.keyUp);
        bindings.add(options.keyLeft);
        bindings.add(options.keyDown);
        bindings.add(options.keyRight);
        bindings.add(options.keyJump);
        bindings.add(options.keyShift);
        bindings.add(options.keySprint);
        bindings.add(options.keyDrop);
        bindings.add(options.keyInventory);
        bindings.add(options.keyChat);
        bindings.add(options.keyPlayerList);
        bindings.add(options.keyPickItem);
        bindings.add(options.keyScreenshot);
        bindings.add(options.keyTogglePerspective);
        bindings.add(options.keySmoothCamera);
        bindings.add(options.keyFullscreen);
        bindings.add(options.keySpectatorOutlines);
        bindings.add(options.keySwapOffhand);
        bindings.add(options.keyAdvancements);
        bindings.addAll(Arrays.asList(options.keyHotbarSlots));
        bindings.add(options.keySocialInteractions);
        return ImmutableList.copyOf(bindings);
    });

    private final Map<String, List<KeyMapping>> categories = new LinkedHashMap<>();

    protected KeyBindingListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(parent, title, itemHeight);
        this.setRowWidth(290);
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
        List<Item> items = new ArrayList<>();

        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Gather all keys bindings and add to corresponding category in map
        Stream.of(Objects.requireNonNull(this.minecraft).options.keyMappings).filter(binding -> !DEFAULT_BINDINGS.contains(binding)).forEach(binding -> {
            this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>()).add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                items.add(new TitleItem(Component.translatable(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                list.forEach(binding -> items.add(new KeyBindingItem(binding)));
            }
        });
        return items;
    }

    protected void onChange() {}

    public class KeyBindingItem extends Item implements ISearchable
    {
        private final KeyMapping mapping;
        private final Button addBinding;
        private final Button removeBinding;

        protected KeyBindingItem(KeyMapping mapping)
        {
            super(Component.translatable(mapping.getName()));
            this.mapping = mapping;
            Collection<KeyAdapterBinding> bindings = Controllable.getBindingRegistry().getKeyAdapters().values();
            this.addBinding = Button.builder(ClientHelper.getIconComponent(Icons.ADD), button -> {
                Controllable.getBindingRegistry().addKeyAdapter(new KeyAdapterBinding(-1, this.mapping));
                KeyBindingItem.this.addBinding.active = false;
                KeyBindingItem.this.removeBinding.active = true;
                KeyBindingListMenuScreen.this.onChange();
            }).size(20, 20).build();
            this.addBinding.setTooltip(Tooltip.create(Component.translatable("controllable.gui.register")));
            this.addBinding.setTooltipDelay(Duration.ofMillis(400));
            this.removeBinding = Button.builder(ClientHelper.getIconComponent(Icons.CROSS), button -> {
                KeyAdapterBinding keyAdapter = Controllable.getBindingRegistry().getKeyAdapterByDescriptionKey(this.mapping.getName() + ".custom");
                if(keyAdapter != null) {
                    Controllable.getBindingRegistry().removeKeyAdapter(keyAdapter);
                    Controllable.getRadialMenu().removeBinding(keyAdapter);
                }
                KeyBindingItem.this.addBinding.active = true;
                KeyBindingItem.this.removeBinding.active = false;
                KeyBindingListMenuScreen.this.onChange();
            }).size(20, 20).build();
            this.removeBinding.setTooltip(Tooltip.create(Component.translatable("controllable.gui.unregister")));
            this.removeBinding.setTooltipDelay(Duration.ofMillis(400));
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyMapping() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyMapping() == this.mapping);

        }

        @Override
        public Component getLabel()
        {
            return this.label;
        }

        public void updateButtons()
        {
            Collection<KeyAdapterBinding> bindings = Controllable.getBindingRegistry().getKeyAdapters().values();
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyMapping() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyMapping() == this.mapping);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.addBinding, this.removeBinding);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            // Draws a transparent black background on every odd item to help match the widgets with the label
            if(index % 2 != 0)
            {
                graphics.fill(left - 2, top - 2, left + rowWidth + 2, top + rowHeight + 2, 0x55000000);
            }
            Controller controller = Controllable.getController();
            if(controller != null && controller.isBeingUsed() && ScreenHelper.isMouseWithin(left, top, rowWidth, rowHeight, mouseX, mouseY))
            {
                ScreenHelper.drawOutlinedBox(graphics, left - 2, top - 2, rowWidth + 4, rowHeight + 4, 0xAAFFFFFF);
            }
            Font font = KeyBindingListMenuScreen.this.minecraft.font;
            graphics.drawString(font, this.label, left + 5, top + 5, 0xFFFFFF);
            this.addBinding.setX(left + rowWidth - 42);
            this.addBinding.setY(top - 1);
            this.addBinding.render(graphics, mouseX, mouseY, partialTicks);
            this.removeBinding.setX(left + rowWidth - 20);
            this.removeBinding.setY(top - 1);
            this.removeBinding.render(graphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public List<? extends NarratableEntry> narratables()
        {
            return ImmutableList.of(new NarratableEntry()
            {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority()
                {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output)
                {
                    output.add(NarratedElementType.TITLE, KeyBindingItem.this.label);
                }
            });
        }
    }
}
