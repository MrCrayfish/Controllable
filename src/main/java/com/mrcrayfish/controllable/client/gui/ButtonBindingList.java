package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends AbstractOptionList<ButtonBindingList.Entry>
{
    private Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);

        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());

        // Add all button bindings to the appropriate category or create a new one
        ButtonBindings.getBindings().stream().filter(binding -> !binding.isReserved()).forEach(binding ->
        {
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                this.addEntry(new CategoryEntry(new TranslationTextComponent(category)));
                list.forEach(binding -> this.addEntry(new BindingEntry(binding)));
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
            this.labelWidth = ButtonBindingList.this.minecraft.fontRenderer.getStringPropertyWidth(this.label);
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
            //System.out.println(x + " " + y + " " + p_230432_4_ + " " + p_230432_5_ + " " + itemHeight + " " + p_230432_7_ + " " + p_230432_8_);
            float labelX = ButtonBindingList.this.minecraft.currentScreen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            ButtonBindingList.this.minecraft.fontRenderer.func_243248_b(matrixStack, this.label, labelX, labelY, 0xFFFFFFFF);
        }
    }

    protected class BindingEntry extends Entry
    {
        private ButtonBinding binding;
        private TextComponent label;
        private ButtonBindingButton bindingButton;
        private ImageButton deleteButton;

        protected BindingEntry(ButtonBinding binding)
        {
            this.binding = binding;
            this.label = new TranslationTextComponent(binding.getDescription());
            this.bindingButton = new ButtonBindingButton(0, 0, binding, button -> {
                //TODO listen for input
            });
            this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button -> {
                binding.reset();
                //TODO fix conflicts
            });
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            ButtonBindingList.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left, y + 6, 0xFFAAAAAA);
            this.bindingButton.x = left + width - 45;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX,mouseY, partialTicks);
            this.deleteButton.x = left + width - 20;
            this.deleteButton.y = y;
            this.deleteButton.active = !this.binding.isDefault();
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
