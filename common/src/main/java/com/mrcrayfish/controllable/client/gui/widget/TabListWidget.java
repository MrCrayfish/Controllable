package com.mrcrayfish.controllable.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.gui.components.TabSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class TabListWidget extends AbstractWidget implements ContainerEventHandler
{
    private final TabSelectionList<?> list;

    public TabListWidget(TabSelectionList<?> list)
    {
        super(0, 0, 100, 0, CommonComponents.EMPTY);
        this.list = list;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.list.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        this.list.updateNarration(output);
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return ImmutableList.of(this.list);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        return this.list.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY)
    {
        return this.list.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event)
    {
        return this.list.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xScroll, double yScroll)
    {
        return this.list.mouseScrolled(mouseX, mouseY, xScroll, yScroll);
    }

    @Override
    public boolean isDragging()
    {
        return this.list.isDragging();
    }

    @Override
    public void setDragging(boolean dragging)
    {
        this.list.setDragging(dragging);
    }

    @Nullable
    @Override
    public GuiEventListener getFocused()
    {
        return this.list.getFocused();
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener)
    {
        this.list.setFocused(listener);
    }

    public void updateDimensions(ScreenRectangle rectangle)
    {
        this.setX(rectangle.left());
        this.setY(rectangle.top());
        this.width = rectangle.width();
        this.height = rectangle.height();
        this.list.updateDimensions(rectangle);
    }
}
