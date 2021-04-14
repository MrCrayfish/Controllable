package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class KeyBindingList extends AbstractOptionList<KeyBindingList.Entry>
{
    private static final List<KeyBinding> DEFAULT_BINDINGS = Util.make(() -> {
        GameSettings settings = Minecraft.getInstance().gameSettings;
        List<KeyBinding> bindings = new ArrayList<>();
        bindings.add(settings.keyBindAttack);
        bindings.add(settings.keyBindUseItem);
        bindings.add(settings.keyBindForward);
        bindings.add(settings.keyBindLeft);
        bindings.add(settings.keyBindBack);
        bindings.add(settings.keyBindRight);
        bindings.add(settings.keyBindJump);
        bindings.add(settings.keyBindSneak);
        bindings.add(settings.keyBindSprint);
        bindings.add(settings.keyBindDrop);
        bindings.add(settings.keyBindInventory);
        bindings.add(settings.keyBindChat);
        bindings.add(settings.keyBindPlayerList);
        bindings.add(settings.keyBindPickBlock);
        bindings.add(settings.keyBindCommand);
        bindings.add(settings.keyBindScreenshot);
        bindings.add(settings.keyBindTogglePerspective);
        bindings.add(settings.keyBindSmoothCamera);
        bindings.add(settings.keyBindFullscreen);
        bindings.add(settings.keyBindSpectatorOutlines);
        bindings.add(settings.keyBindSwapHands);
        bindings.add(settings.keyBindSaveToolbar);
        bindings.add(settings.keyBindLoadToolbar);
        bindings.add(settings.keyBindAdvancements);
        bindings.addAll(Arrays.asList(settings.keyBindsHotbar));
        return ImmutableList.copyOf(bindings);
    });

    private SelectKeyBindingScreen parent;
    private Map<String, List<KeyBinding>> categories = new LinkedHashMap<>();

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

        Stream.of(this.minecraft.gameSettings.keyBindings).filter(binding -> !DEFAULT_BINDINGS.contains(binding)).forEach(binding -> {
            this.categories.computeIfAbsent(binding.getKeyCategory(), category -> new ArrayList<>()).add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                this.addEntry(new CategoryEntry(new TranslationTextComponent(category)));
                list.forEach(binding -> this.addEntry(new KeyBindingEntry(binding)));
            }
        });
    }

    abstract class Entry extends AbstractOptionList.Entry<Entry> {}

    protected class CategoryEntry extends Entry
    {
        private final ITextComponent label;
        private final int labelWidth;

        protected CategoryEntry(ITextComponent label)
        {
            this.label = label;
            this.labelWidth = KeyBindingList.this.minecraft.fontRenderer.getStringPropertyWidth(this.label);
        }

        @Override
        public boolean changeFocus(boolean focus)
        {
            return false;
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return Collections.emptyList();
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int y, int p_230432_4_, int p_230432_5_, int itemHeight, int p_230432_7_, int p_230432_8_, boolean selected, float partialTicks)
        {
            float labelX = KeyBindingList.this.minecraft.currentScreen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            KeyBindingList.this.minecraft.fontRenderer.func_243248_b(matrixStack, this.label, labelX, labelY, 0xFFFFFFFF);
        }
    }

    public class KeyBindingEntry extends Entry
    {
        private KeyBinding binding;
        private TextComponent label;
        private Button addBinding;
        private Button removeBinding;

        protected KeyBindingEntry(KeyBinding binding)
        {
            this.binding = binding;
            this.label = new TranslationTextComponent(binding.getKeyDescription());
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
            this.addBinding = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
            {
                BindingRegistry.getInstance().addKeyAdapter(new KeyAdapterBinding(-1, this.binding));
                this.addBinding.active = false;
                this.removeBinding.active = true;
                KeyBindingList.this.parent.updateButtons();
            });
            this.removeBinding = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 15, 10, 10, button ->
            {
                KeyAdapterBinding keyAdapter = BindingRegistry.getInstance().getKeyAdapterByDescriptionKey(this.binding.getKeyDescription() + ".custom");
                if(keyAdapter != null)
                {
                    BindingRegistry.getInstance().removeKeyAdapter(keyAdapter);
                    RadialMenuHandler.instance().removeBinding(keyAdapter);
                }
                this.addBinding.active = true;
                this.removeBinding.active = false;
                KeyBindingList.this.parent.updateButtons();
            });
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyBinding() == this.binding);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyBinding() == this.binding);
        }

        public void updateButtons()
        {
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyBinding() == this.binding);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyBinding() == this.binding);
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return ImmutableList.of(this.addBinding, this.removeBinding);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(MatrixStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            KeyBindingList.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left - 15, y + 6, TextFormatting.GRAY.getColor());
            this.addBinding.x = left + width - 38;
            this.addBinding.y = y;
            this.addBinding.render(matrixStack, mouseX, mouseY, partialTicks);
            this.removeBinding.x = left + width - 15;
            this.removeBinding.y = y;
            this.removeBinding.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
