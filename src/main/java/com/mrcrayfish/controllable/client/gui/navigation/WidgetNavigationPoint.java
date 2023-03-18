package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.client.gui.components.AbstractWidget;

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
}
