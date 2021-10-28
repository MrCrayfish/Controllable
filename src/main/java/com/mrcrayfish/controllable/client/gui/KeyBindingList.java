package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class KeyBindingList extends AbstractSelectionList<KeyBindingList.Entry>
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

    private SelectKeyBindingScreen parent;
    private Map<String, List<KeyMapping>> categories = new LinkedHashMap<>();

    public KeyBindingList(SelectKeyBindingScreen parent, Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);
        this.parent = parent;
        this.updateList(false);
    }

    public void updateList(boolean showUnbound)
    {
        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());

        Stream.of(this.minecraft.options.keyMappings).filter(binding -> !DEFAULT_BINDINGS.contains(binding)).forEach(binding -> {
            this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>()).add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                this.addEntry(new CategoryEntry(new TranslatableComponent(category)));
                list.forEach(binding -> this.addEntry(new KeyBindingEntry(binding)));
            }
        });
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput)
    {

    }

    abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {}

    protected class CategoryEntry extends Entry
    {
        private final Component label;
        private final int labelWidth;

        protected CategoryEntry(Component label)
        {
            this.label = label;
            this.labelWidth = KeyBindingList.this.minecraft.font.width(this.label);
        }

        @Override
        public boolean changeFocus(boolean focus)
        {
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return Collections.emptyList();
        }

        @Override
        public void render(PoseStack poseStack, int x, int y, int p_230432_4_, int p_230432_5_, int itemHeight, int p_230432_7_, int p_230432_8_, boolean selected, float partialTicks)
        {
            float labelX = KeyBindingList.this.minecraft.screen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            KeyBindingList.this.minecraft.font.draw(poseStack, this.label, labelX, labelY, 0xFFFFFFFF);
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
                    output.add(NarratedElementType.TITLE, CategoryEntry.this.label);
                }
            });
        }
    }

    public class KeyBindingEntry extends Entry
    {
        private KeyMapping mapping;
        private Component label;
        private Button addBinding;
        private Button removeBinding;

        protected KeyBindingEntry(KeyMapping mapping)
        {
            this.mapping = mapping;
            this.label = new TranslatableComponent(mapping.getName());
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
            this.addBinding = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
            {
                BindingRegistry.getInstance().addKeyAdapter(new KeyAdapterBinding(-1, this.mapping));
                this.addBinding.active = false;
                this.removeBinding.active = true;
                KeyBindingList.this.parent.updateButtons();
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
                KeyBindingList.this.parent.updateButtons();
            });
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyMapping() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyMapping() == this.mapping);
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
            KeyBindingList.this.minecraft.font.draw(matrixStack, this.label, left - 15, y + 6, ChatFormatting.GRAY.getColor());
            this.addBinding.x = left + width - 38;
            this.addBinding.y = y;
            this.addBinding.render(matrixStack, mouseX, mouseY, partialTicks);
            this.removeBinding.x = left + width - 15;
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
                    output.add(NarratedElementType.TITLE, KeyBindingEntry.this.label);
                }
            });
        }
    }
}
