package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.widget.ColorButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.time.Duration;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RadialItemList extends AbstractSelectionList<RadialItemList.ButtonBindingEntry>
{
    private final List<RadialMenuAction> bindings;
    private ButtonBinding selectedBinding;

    public RadialItemList(Minecraft mc, List<RadialMenuAction> bindings)
    {
        super(mc, 0, 0, 0, 36);
        this.bindings = bindings;
        this.updateEntries();
    }

    public void updateEntries()
    {
        this.clearEntries();

        this.bindings.forEach(binding -> {
            this.addEntry(new ButtonBindingEntry(binding));
        });

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
        return 340;
    }

    @Override
    public int getRowLeft()
    {
        return super.getRowLeft();
    }

    @Override
    protected int scrollBarX()
    {
        return super.getRowRight() + 2;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    @Override
    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        List<RadialItemList.ButtonBindingEntry> entries = this.children();
        for(int i = 0; i < entries.size(); i++)
        {
            var entry = entries.get(i);
            if(entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom())
            {
                if(i % 2 != 0)
                {
                    graphics.fill(entry.getX(), entry.getY(), entry.getX() + entry.getWidth(), entry.getY() + entry.getHeight(), 0x55000000);
                }
                this.renderItem(graphics, mouseX, mouseY, partialTick, entry);
            }
        }
    }

    @Override
    protected void renderSelection(GuiGraphics graphics, ButtonBindingEntry entry, int outlineColour)
    {
        int left = entry.getX();
        int right = entry.getX() + entry.getWidth();
        int top = entry.getY();
        int bottom = entry.getY() + entry.getHeight();
        graphics.fill(left, top, right, bottom, outlineColour);
        graphics.fill(left + 1, top + 1, right - 1, bottom -1, 0xFF111111);
    }

    public class ButtonBindingEntry extends ContainerObjectSelectionList.Entry<ButtonBindingEntry>
    {
        private final RadialMenuAction data;
        private final Component description;
        private final ColorButton colorButton;
        private final Button moveUpButton;
        private final Button moveDownButton;
        private final Button deleteButton;
        private final Button[] buttons;
        private Component label;

        public ButtonBindingEntry(RadialMenuAction data)
        {
            this.data = data;
            this.description = Component.translatable(data.getBinding().getCategory());
            this.moveUpButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 35, 10, 10, button -> {
                this.shiftBinding(false);
            });
            this.moveUpButton.setTooltip(Tooltip.create(Component.translatable("controllable.gui.shift_up")));
            this.moveUpButton.setTooltipDelay(Duration.ofMillis(400));
            this.moveDownButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 35, 10, 10, button -> {
                this.shiftBinding(true);
            });
            this.moveDownButton.setTooltip(Tooltip.create(Component.translatable("controllable.gui.shift_down")));
            this.moveDownButton.setTooltipDelay(Duration.ofMillis(400));
            this.label = Component.translatable(data.getBinding().getLabelKey()).withStyle(data.getColor());
            this.colorButton = new ColorButton(0, 0, button -> {
                data.setColor(((ColorButton) button).getColor());
                this.label = this.label.copy().withStyle(((ColorButton) button).getColor());
            });
            this.colorButton.setColor(data.getColor());
            this.colorButton.setTooltip(Tooltip.create(Component.translatable("controllable.gui.change_color")));
            this.colorButton.setTooltipDelay(Duration.ofMillis(400));
            this.deleteButton = Button.builder(ClientHelper.getIconComponent(Icons.CROSS), button -> {
                RadialItemList.this.bindings.remove(data);
                RadialItemList.this.removeEntry(this);
                RadialItemList.this.setScrollAmount(RadialItemList.this.scrollAmount());
                RadialItemList.this.children().forEach(ButtonBindingEntry::updateButtons);
            }).size(20, 20).build();
            this.deleteButton.setTooltip(Tooltip.create(Component.translatable("controllable.gui.delete")));
            this.deleteButton.setTooltipDelay(Duration.ofMillis(400));
            this.buttons = new Button[]{this.colorButton, this.moveDownButton, this.moveUpButton, this.deleteButton};
            this.updateButtons();
        }

        public RadialMenuAction getData()
        {
            return this.data;
        }

        private void shiftBinding(boolean down)
        {
            List<RadialMenuAction> bindings = RadialItemList.this.bindings;
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
            return ImmutableList.of(this.colorButton, this.moveUpButton, this.moveDownButton, this.deleteButton);
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick)
        {
            Font font = RadialItemList.this.minecraft.font;
            graphics.drawString(font, this.label, this.getX() + 5, this.getY() + 7, 0xFFFFFFFF);
            graphics.drawString(font, this.description, this.getX() + 5, this.getY() + 20, 0xFFFFFFFF);
            for(int i = 0; i < this.buttons.length; i++)
            {
                int offset = (this.buttons.length - i) * 22;
                int buttonLeft = this.getX() + this.getWidth() - 6 - offset;
                this.buttons[i].visible = RadialItemList.this.getSelected() == this;
                this.buttons[i].setX(buttonLeft);
                this.buttons[i].setY(this.getY() + 8);
                this.buttons[i].render(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
        {
            RadialItemList.this.setSelected(this);
            RadialItemList.this.selectedBinding = this.data.getBinding();
            return super.mouseClicked(event, doubleClick);
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
