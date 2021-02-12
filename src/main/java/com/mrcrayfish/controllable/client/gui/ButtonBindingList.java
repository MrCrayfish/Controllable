package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends AbstractOptionList<ButtonBindingList.Entry>
{
    private ButtonBindingScreen screen;
    private Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(ButtonBindingScreen screen, Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);
        this.screen = screen;

        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());

        // Add all button bindings to the appropriate category or create a new one
        BindingRegistry.getInstance().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.screen.isWaitingForButtonInput())
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract class Entry extends AbstractOptionList.Entry<Entry> {}

    protected class CategoryEntry extends Entry
    {
        private final ITextComponent label;
        private final int labelWidth;

        protected CategoryEntry(ITextComponent label)
        {
            this.label = label;
            this.labelWidth = ButtonBindingList.this.minecraft.fontRenderer.getStringWidth(this.label.getFormattedText());
        }

        @Override
        public boolean changeFocus(boolean focus)
        {
            return false;
        }

        @Override
        public List<? extends IGuiEventListener> children()
        {
            return Collections.emptyList();
        }

        @Override
        public void render(int x, int y, int p_230432_4_, int p_230432_5_, int itemHeight, int p_230432_7_, int p_230432_8_, boolean selected, float partialTicks)
        {
            float labelX = ButtonBindingList.this.minecraft.currentScreen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            ButtonBindingList.this.minecraft.fontRenderer.drawString(this.label.getFormattedText(), labelX, labelY, 0xFFFFFFFF);
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
                ButtonBindingList.this.screen.setSelectedBinding(this.binding);
            });
            this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button -> {
                binding.reset();
                BindingRegistry registry = BindingRegistry.getInstance();
                registry.resetBindingHash();
                registry.save();
            });
        }

        @Override
        public List<? extends IGuiEventListener> children()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            int color = this.binding.isConflictingContext() ? TextFormatting.RED.getColor() : TextFormatting.GRAY.getColor();
            ButtonBindingList.this.minecraft.fontRenderer.drawString(this.label.getFormattedText(), left - 15, y + 6, color);
            this.bindingButton.x = left + width - 45;
            this.bindingButton.y = y;
            this.bindingButton.render(mouseX,mouseY, partialTicks);
            this.deleteButton.x = left + width - 20;
            this.deleteButton.y = y;
            this.deleteButton.active = !this.binding.isDefault();
            this.deleteButton.render(mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && this.bindingButton.isHovered())
            {
                this.binding.setButton(-1);
                this.bindingButton.playDownSound(Minecraft.getInstance().getSoundHandler());
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
