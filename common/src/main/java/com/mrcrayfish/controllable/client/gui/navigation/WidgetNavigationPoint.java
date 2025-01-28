package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TabButton;

/**
 * Author: MrCrayfish
 */
public class WidgetNavigationPoint extends NavigationPoint
{
    private final AbstractWidget widget;

    public WidgetNavigationPoint(double x, double y, AbstractWidget widget)
    {
        super(x, y, Type.WIDGET);
        this.widget = widget;
    }

    public AbstractWidget getWidget()
    {
        return this.widget;
    }

    @Override
    public boolean shouldHide()
    {
        return this.widget instanceof AbstractButton || this.widget instanceof TabButton || this.widget instanceof HideCursor;
    }
}
