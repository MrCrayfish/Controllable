package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public abstract class KeyBindingListMenuScreen extends ListMenuScreen
{
    private static final List<KeyBinding> DEFAULT_BINDINGS = Util.make(() -> {
        GameSettings options = Minecraft.getInstance().gameSettings;
        List<KeyBinding> bindings = new ArrayList<>();
        bindings.add(options.keyBindAttack);
        bindings.add(options.keyBindUseItem);
        bindings.add(options.keyBindForward);
        bindings.add(options.keyBindLeft);
        bindings.add(options.keyBindBack);
        bindings.add(options.keyBindRight);
        bindings.add(options.keyBindJump);
        bindings.add(options.keyBindSneak);
        bindings.add(options.keyBindSprint);
        bindings.add(options.keyBindDrop);
        bindings.add(options.keyBindInventory);
        bindings.add(options.keyBindChat);
        bindings.add(options.keyBindPlayerList);
        bindings.add(options.keyBindPickBlock);
        bindings.add(options.keyBindCommand);
        bindings.add(options.keyBindScreenshot);
        bindings.add(options.keyBindTogglePerspective);
        bindings.add(options.keyBindSmoothCamera);
        bindings.add(options.keyBindFullscreen);
        bindings.add(options.keyBindSpectatorOutlines);
        bindings.add(options.keyBindSwapHands);
        bindings.add(options.keyBindSaveToolbar);
        bindings.add(options.keyBindLoadToolbar);
        bindings.add(options.keyBindAdvancements);
        bindings.addAll(Arrays.asList(options.keyBindsHotbar));
        return ImmutableList.copyOf(bindings);
    });

    private Map<String, List<KeyBinding>> categories = new LinkedHashMap<>();

    protected KeyBindingListMenuScreen(Screen parent, TextComponent title, int itemHeight)
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

        Stream.of(this.minecraft.gameSettings.keyBindings).filter(binding -> !DEFAULT_BINDINGS.contains(binding)).forEach(binding -> {
            this.categories.computeIfAbsent(binding.getKeyCategory(), category -> new ArrayList<>()).add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                entries.add(new TitleItem(new TranslationTextComponent(category).mergeStyle(TextFormatting.YELLOW, TextFormatting.BOLD)));
                list.forEach(binding -> entries.add(new KeyBindingItem(binding)));
            }
        });
    }

    protected void onChange() {}

    public class KeyBindingItem extends Item implements ISearchable
    {
        private final KeyBinding mapping;
        private Button addBinding;
        private Button removeBinding;

        protected KeyBindingItem(KeyBinding mapping)
        {
            super(new TranslationTextComponent(mapping.getTranslationKey()));
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
                KeyAdapterBinding keyAdapter = BindingRegistry.getInstance().getKeyAdapterByDescriptionKey(this.mapping.getKeyDescription() + ".custom");
                if(keyAdapter != null)
                {
                    BindingRegistry.getInstance().removeKeyAdapter(keyAdapter);
                    RadialMenuHandler.instance().removeBinding(keyAdapter);
                }
                this.addBinding.active = true;
                this.removeBinding.active = false;
                KeyBindingListMenuScreen.this.onChange();
            });
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyBinding() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyBinding() == this.mapping);
        }

        @Override
        public String getLabel()
        {
            return this.label.copyRaw().getString();
        }

        public void updateButtons()
        {
            Collection<KeyAdapterBinding> bindings = BindingRegistry.getInstance().getKeyAdapters().values();
            this.addBinding.active = bindings.stream().noneMatch(entry -> entry.getKeyBinding() == this.mapping);
            this.removeBinding.active = bindings.stream().anyMatch(entry -> entry.getKeyBinding() == this.mapping);
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
            KeyBindingListMenuScreen.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left, y + 6, 0xFFFFFF);
            this.addBinding.x = left + width - 42;
            this.addBinding.y = y;
            this.addBinding.render(matrixStack, mouseX, mouseY, partialTicks);
            this.removeBinding.x = left + width - 20;
            this.removeBinding.y = y;
            this.removeBinding.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}