package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.client.gui.GuiButton;

/**
 * Author: MrCrayfish
 */
public class WidgetNavigationPoint extends NavigationPoint
{
    private GuiButton widget;

    public WidgetNavigationPoint(double x, double y, GuiButton widget)
    {
        super(x, y, Type.WIDGET);
        this.widget = widget;
    }

    public GuiButton getWidget()
    {
        return this.widget;
    }
}
