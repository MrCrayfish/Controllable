package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class TabSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> implements LayoutElement
{
    protected Component headerText;
    protected Component footerText;

    public TabSelectionList(Minecraft mc, int itemHeight)
    {
        super(mc, 0, 0, 0, 0, itemHeight);
        this.setRenderTopAndBottom(false);
    }

    public TabSelectionList<E> setHeaderText(Component headerText)
    {
        this.headerText = headerText;
        return this;
    }

    public TabSelectionList<E> setFooterText(Component footerText)
    {
        this.footerText = footerText;
        return this;
    }

    @Override
    public int getRowWidth()
    {
        return 290;
    }

    @Override
    protected int getScrollbarPosition()
    {
        return this.getRowLeft() + this.getRowWidth() + 4;
    }

    @Override
    public int addEntry(E entry)
    {
        return super.addEntry(entry);
    }

    @Override
    public void setX(int x)
    {
        this.x0 = x;
        this.x1 = x + this.width;
    }

    @Override
    public void setY(int y)
    {
        this.y0 = y;
        this.y1 = y + this.height;
    }

    @Override
    public int getX()
    {
        return this.x0;
    }

    @Override
    public int getY()
    {
        return this.y0;
    }

    @Override
    public int getWidth()
    {
        return this.width;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    public void updateDimensions(ScreenRectangle rectangle)
    {
        boolean header = this.headerText != null;
        boolean footer = this.footerText != null;
        this.width = rectangle.width();
        this.height = rectangle.height() - 15 + (header ? -10 : 0) + (footer ? -20 : 0);
        this.setX(rectangle.left());
        this.setY(rectangle.top() + 15 + (header ? 10 : 0));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        super.render(poseStack, mouseX, mouseY, partialTick);
        if(this.headerText != null)
        {
            GuiComponent.drawCenteredString(poseStack, TabSelectionList.this.minecraft.font, this.headerText, this.x0 + this.width / 2, this.y0 - 15, 0xFFFFFF);
        }
        if(this.footerText != null)
        {
            Font font = TabSelectionList.this.minecraft.font;
            int footerWidth = font.width(this.footerText);
            ScreenUtil.drawRoundedBox(poseStack, this.x0 + (this.width - footerWidth) / 2, this.y1 + 4, footerWidth, 14, 0x55000000);
            GuiComponent.drawCenteredString(poseStack, font, this.footerText, this.x0 + this.width / 2, this.y1 + 7, 0xFFFFFF);
        }
        fillGradient(poseStack, this.x0, this.y0, this.x1, this.y0 + 4, 0xFF000000, 0);
        fillGradient(poseStack, this.x0, this.y1 - 4, this.x1, this.y1, 0, 0xFF000000);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {}

    @Override
    public void updateNarration(NarrationElementOutput output) {}

    public abstract static class Item<T extends ContainerObjectSelectionList.Entry<T>> extends ContainerObjectSelectionList.Entry<T>
    {
        protected Component label;

        public Item(Component label)
        {
            this.label = label;
        }

        public Component getLabel()
        {
            return this.label;
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
                    output.add(NarratedElementType.TITLE, Item.this.label);
                }
            });
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return Collections.emptyList();
        }
    }

    public abstract static class BaseItem extends Item<BaseItem>
    {
        public BaseItem(Component label)
        {
            super(label);
        }

        @Override
        public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
        {

        }
    }

    public class TitleItem extends BaseItem implements SkipItem
    {
        public TitleItem(Component title)
        {
            super(title);
        }

        public TitleItem(String title)
        {
            super(Component.literal(title).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW));
        }

        @Override
        public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            Font font = Objects.requireNonNull(TabSelectionList.this.minecraft).font;
            int labelWidth = font.width(this.label) + 2;
            ScreenUtil.drawRoundedBox(poseStack, left + width / 2 - labelWidth / 2, top + 2, labelWidth, 14, 0x88000000);
            Screen.drawCenteredString(poseStack, font, this.label, left + width / 2, top + 5, 0xFFFFFF);
        }

        @Override
        public List<? extends NarratableEntry> narratables()
        {
            return Collections.emptyList();
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return Collections.emptyList();
        }
    }

    public static class ButtonItem extends BaseItem implements Navigatable
    {
        private final Button button;

        public ButtonItem(Component label, Button.OnPress onPress)
        {
            super(label);
            this.button = Button.builder(label, onPress).build();
        }

        @Override
        public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.button.setWidth(width / 2);
            this.button.setX(left + width / 4);
            this.button.setY(top);
            this.button.render(poseStack, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.button);
        }

        @Override
        public List<GuiEventListener> elements()
        {
            return Collections.emptyList();
        }
    }
}
