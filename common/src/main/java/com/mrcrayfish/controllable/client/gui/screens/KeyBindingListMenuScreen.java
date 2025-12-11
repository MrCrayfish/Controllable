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
import net.minecraft.util.Util;
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
import net.minecraft.resources.Identifier;

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

    private final Map<KeyMapping.Category, List<KeyMapping>> categories = new LinkedHashMap<>();

    protected KeyBindingListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(parent, title, itemHeight);
        this.setRowWidth(290);
        this.categories.put(KeyMapping.Category.MOVEMENT, new ArrayList<>());
        this.categories.put(KeyMapping.Category.GAMEPLAY, new ArrayList<>());
        this.categories.put(KeyMapping.Category.INVENTORY, new ArrayList<>());
        this.categories.put(KeyMapping.Category.CREATIVE, new ArrayList<>());
        this.categories.put(KeyMapping.Category.MULTIPLAYER, new ArrayList<>());
        this.categories.put(KeyMapping.Category.SPECTATOR, new ArrayList<>());
        this.categories.put(KeyMapping.Category.MISC, new ArrayList<>());
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
                items.add(new TitleItem(category.label().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
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
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick)
        {
            Controller controller = Controllable.getController();
            if(controller != null && controller.isBeingUsed() && ScreenHelper.isMouseWithin(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY))
            {
                ScreenHelper.drawOutlinedBox(graphics, this.getX() - 2, this.getY() - 1, this.getWidth() + 4, this.getHeight() + 2, 0xAAFFFFFF);
            }
            Font font = KeyBindingListMenuScreen.this.minecraft.font;
            graphics.drawString(font, this.label, this.getX() + 5, this.getY() + 7, 0xFFFFFFFF);
            this.addBinding.setX(this.getX() + this.getWidth() - 42);
            this.addBinding.setY(this.getY() + 1);
            this.addBinding.render(graphics, mouseX, mouseY, partialTick);
            this.removeBinding.setX(this.getX() + this.getWidth() - 20);
            this.removeBinding.setY(this.getY() + 1);
            this.removeBinding.render(graphics, mouseX, mouseY, partialTick);
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
