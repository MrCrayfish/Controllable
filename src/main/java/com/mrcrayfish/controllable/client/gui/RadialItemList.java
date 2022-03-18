package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.ColorButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RadialItemList extends AbstractSelectionList<RadialItemList.ButtonBindingEntry>
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
        ButtonBindingEntry selected = this.children().stream().filter(data -> data.getData().getBinding() == this.selectedBinding).findFirst().orElse(null);
        if(selected == null && this.children().size() > 0)
        {
            selected = this.children().get(0);
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
    public int getLeft()
    {
        return super.getLeft();
    }

    @Override
    public int getRowLeft()
    {
        if(true) return super.getRowLeft();
        return this.x0 + this.width / 2 - this.getRowWidth() / 2;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {}

    class ButtonBindingEntry extends ContainerObjectSelectionList.Entry<ButtonBindingEntry>
    {
        private final ButtonBindingData data;
        private Component label;
        private Component description;
        private ColorButton colorButton;
        private Button moveUpButton;
        private Button moveDownButton;

        public ButtonBindingEntry(ButtonBindingData data)
        {
            this.data = data;
            this.label = new TranslatableComponent(data.getBinding().getLabelKey()).withStyle(data.getColor());
            this.description = new TranslatableComponent(data.getBinding().getCategory());
            this.colorButton = new ColorButton(0, 0, button -> {
                data.setColor(this.colorButton.getColor());
                this.label = this.label.copy().withStyle(this.colorButton.getColor());
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
            index = Mth.clamp(index + (down ? 1 : -1), 0, bindings.size());
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
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.colorButton, this.moveUpButton, this.moveDownButton);
        }

        @Override
        public void render(PoseStack poseStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            RadialItemList.this.minecraft.font.draw(poseStack, this.label, left + 5, top + 5, 0xFFFFFF);
            RadialItemList.this.minecraft.font.draw(poseStack, this.description, left + 5, top + 18, 0xFFFFFF);
            this.colorButton.visible = RadialItemList.this.getSelected() == this;
            this.colorButton.x = left + RadialItemList.this.getRowWidth() - 78;
            this.colorButton.y = top + 6;
            this.colorButton.render(poseStack, mouseX, mouseY, partialTicks);
            this.moveUpButton.visible = RadialItemList.this.getSelected() == this;
            this.moveUpButton.x = left + RadialItemList.this.getRowWidth() - 34;
            this.moveUpButton.y = top + 6;
            this.moveUpButton.render(poseStack, mouseX, mouseY, partialTicks);
            this.moveDownButton.visible = RadialItemList.this.getSelected() == this;
            this.moveDownButton.x = left + RadialItemList.this.getRowWidth() - 56;
            this.moveDownButton.y = top + 6;
            this.moveDownButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            RadialItemList.this.setSelected(this);
            RadialItemList.this.selectedBinding = this.data.getBinding();
            return super.mouseClicked(mouseX, mouseY, button);
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
                    output.add(NarratedElementType.TITLE, ButtonBindingEntry.this.label);
                }
            });
        }
    }
}
