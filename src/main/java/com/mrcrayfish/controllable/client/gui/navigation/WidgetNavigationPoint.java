package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.client.gui.components.Widget;

/**
 * Author: MrCrayfish
 */
public class WidgetNavigationPoint extends NavigationPoint
{
    private Widget widget;

    public WidgetNavigationPoint(double x, double y, Widget widget)
    {
        super(x, y, Type.WIDGET);
        this.widget = widget;
    }

    public Widget getWidget()
    {
        return this.widget;
    }
}
