package com.mrcrayfish.controllable.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.gui.components.TabSelectionList;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class TabListWidget extends AbstractWidget implements ContainerEventHandler, Navigatable
{
    private final Supplier<ScreenRectangle> dimensions;
    private final TabSelectionList list;

    public TabListWidget(Supplier<ScreenRectangle> dimensions, TabSelectionList list)
    {
        super(0, 0, 100, 0, CommonComponents.EMPTY);
        this.dimensions = dimensions;
        this.list = list;
    }

    @Override
    public boolean canNavigate()
    {
        return false;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        ScreenRectangle dimensions = this.dimensions.get();
        this.setX(dimensions.left());
        this.setY(dimensions.top());
        this.width = dimensions.width();
        this.height = dimensions.height();
        this.list.updateDimensions(dimensions);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return this.list.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double $$0, double $$1, int $$2, double $$3, double $$4)
    {
        return this.list.mouseDragged($$0, $$1, $$2, $$3, $$4);
    }

    @Override
    public boolean mouseReleased(double $$0, double $$1, int $$2)
    {
        return this.list.mouseReleased($$0, $$1, $$2);
    }

    @Override
    public boolean mouseScrolled(double $$0, double $$1, double $$2)
    {
        return this.list.mouseScrolled($$0, $$1, $$2);
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
}
