package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
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
        bindings.add(options.keyCommand);
        bindings.add(options.keyScreenshot);
        bindings.add(options.keyTogglePerspective);
        bindings.add(options.keySmoothCamera);
        bindings.add(options.keyFullscreen);
        bindings.add(options.keySpectatorOutlines);
        bindings.add(options.keySwapOffhand);
        bindings.add(options.keySaveHotbarActivator);
        bindings.add(options.keyLoadHotbarActivator);
        bindings.add(options.keyAdvancements);
        bindings.addAll(Arrays.asList(options.keyHotbarSlots));
        return ImmutableList.copyOf(bindings);
    });

    private Map<String, List<KeyMapping>> categories = new LinkedHashMap<>();

    protected KeyBindingListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(parent, title, itemHeight);
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        this.updateList(entries, false);
    }

    public void updateList(List<Item> entries, boolean showUnbound)
    {
        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Gather all keys bindings and add to corresponding category in map
        Stream.of(this.minecraft.options.keyMappings).filter(binding -> !DEFAULT_BINDINGS.contains(binding)).forEach(binding -> {
            this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>()).add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                entries.add(new TitleItem(Component.translatable(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                list.forEach(binding -> entries.add(new KeyBindingItem(binding)));
            }
        });
    }

    protected void onChange() {}

    public class KeyBindingItem extends Item implements ISearchable
    {
        private final KeyMapping mapping;
        private Button addBinding;
        private Button removeBinding;

        protected KeyBindingItem(KeyMapping mapping)
        {
            super(Component.translatable(mapping.getName()));
            this.mapping = mapping;
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
            this.addBinding = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
            {
                BindingRegistry.getInstance().addKeyAdapter(new KeyAdapterBinding(-1, this.mapping));
                this.addBinding.active = false;
                this.removeBinding.active = true;
                KeyBindingListMenuScreen.this.onChange();
            });
            this.removeBinding = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 15, 10, 10, button ->
            {
                KeyAdapterBinding keyAdapter = BindingRegistry.getInstance().getKeyAdapterByDescriptionKey(this.mapping.getName() + ".custom");
                if(keyAdapter != null)
                {
                    BindingRegistry.getInstance().removeKeyAdapter(keyAdapter);
                    RadialMenuHandler.instance().removeBinding(keyAdapter);
                }
                this.addBinding.active = true;
                this.removeBinding.active = false;
                KeyBindingListMenuScreen.this.onChange();
            });
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyMapping() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyMapping() == this.mapping);
        }

        @Override
        public String getLabel()
        {
            return this.label.plainCopy().getString();
        }

        public void updateButtons()
        {
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
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
        public void render(PoseStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            KeyBindingListMenuScreen.this.minecraft.font.draw(matrixStack, this.label, left, y + 6, 0xFFFFFF);
            this.addBinding.x = left + width - 42;
            this.addBinding.y = y;
            this.addBinding.render(matrixStack, mouseX, mouseY, partialTicks);
            this.removeBinding.x = left + width - 20;
            this.removeBinding.y = y;
            this.removeBinding.render(matrixStack, mouseX, mouseY, partialTicks);
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
