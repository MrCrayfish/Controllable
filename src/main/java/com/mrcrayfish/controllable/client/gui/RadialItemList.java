package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.ColorButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RadialItemList extends ExtendedList<RadialItemList.ButtonBindingEntry>
{
    private List<ButtonBindingData> bindings;
    private ButtonBinding selectedBinding;

    public RadialItemList(Minecraft mc, int width, int height, int top, int bottom, List<ButtonBindingData> bindings)
    {
        super(mc, width, height, top, bottom, 36);
        this.bindings = bindings;
        this.updateEntries();
    }

    private void updateEntries()
    {
        this.clearEntries();
        this.bindings.forEach(binding -> this.addEntry(new ButtonBindingEntry(binding)));

        // Update the selected entry
        ButtonBindingEntry selected = this.getEventListeners().stream().filter(data -> data.getData().getBinding() == this.selectedBinding).findFirst().orElse(null);
        if(selected == null && this.getEventListeners().size() > 0)
        {
            selected = this.getEventListeners().get(0);
            this.selectedBinding = selected.getData().getBinding();
        }
        this.setSelected(selected);

    }

    @Override
    public int getRowWidth()
    {
        return 260;
    }

    @Override
    protected int getRowLeft()
    {
        if(true) return super.getRowLeft();
        return this.x0 + this.width / 2 - this.getRowWidth() / 2;
    }

    class ButtonBindingEntry extends AbstractOptionList.Entry<ButtonBindingEntry>
    {
        private final ButtonBindingData data;
        private ITextComponent label;
        private ITextComponent description;
        private ColorButton colorButton;
        private Button moveUpButton;
        private Button moveDownButton;

        public ButtonBindingEntry(ButtonBindingData data)
        {
            this.data = data;
            this.label = new TranslationTextComponent(data.getBinding().getLabelKey()).mergeStyle(data.getColor());
            this.description = new TranslationTextComponent(data.getBinding().getCategory());
            this.colorButton = new ColorButton(0, 0, button -> {
                data.setColor(this.colorButton.getColor());
                this.label = this.label.copyRaw().mergeStyle(this.colorButton.getColor());
            });
            this.colorButton.setColor(data.getColor());
            this.moveUpButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 35, 10, 10, button -> {
                this.shiftBinding(false);
            });
            this.moveDownButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 35, 10, 10, button -> {
                this.shiftBinding(true);
            });
            this.updateButtons();
        }

        public ButtonBindingData getData()
        {
            return this.data;
        }

        private void shiftBinding(boolean down)
        {
            List<ButtonBindingData> bindings = RadialItemList.this.bindings;
            int index = bindings.indexOf(this.data);
            bindings.remove(this.data);
            index = MathHelper.clamp(index + (down ? 1 : -1), 0, bindings.size());
            bindings.add(index, this.data);
            this.updateButtons();
            RadialItemList.this.updateEntries();
        }

        private void updateButtons()
        {
            int index = RadialItemList.this.bindings.indexOf(this.data);
            this.moveUpButton.active = index > 0;
            this.moveDownButton.active = index < bindings.size() - 1;
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return ImmutableList.of(this.colorButton, this.moveUpButton, this.moveDownButton);
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            RadialItemList.this.minecraft.fontRenderer.func_243248_b(matrixStack, this.label, left + 5, top + 5, 0xFFFFFF);
            RadialItemList.this.minecraft.fontRenderer.func_243248_b(matrixStack, this.description, left + 5, top + 18, 0xFFFFFF);
            this.colorButton.visible = RadialItemList.this.getSelected() == this;
            this.colorButton.x = left + RadialItemList.this.getRowWidth() - 78;
            this.colorButton.y = top + 6;
            this.colorButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.moveUpButton.visible = RadialItemList.this.getSelected() == this;
            this.moveUpButton.x = left + RadialItemList.this.getRowWidth() - 34;
            this.moveUpButton.y = top + 6;
            this.moveUpButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.moveDownButton.visible = RadialItemList.this.getSelected() == this;
            this.moveDownButton.x = left + RadialItemList.this.getRowWidth() - 56;
            this.moveDownButton.y = top + 6;
            this.moveDownButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            RadialItemList.this.setSelected(this);
            RadialItemList.this.selectedBinding = this.data.getBinding();
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
