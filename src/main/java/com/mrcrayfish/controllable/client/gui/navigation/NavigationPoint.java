package com.mrcrayfish.controllable.client.gui.navigation;

/**
 * Author: MrCrayfish
 */
public abstract class NavigationPoint
{
    private final double x, y;
    private final Type type;

    public NavigationPoint(double x, double y, Type type)
    {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public double distanceTo(double x, double y)
    {
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public Type getType()
    {
        return this.type;
    }

    protected enum Type
    {
        BASIC,
        WIDGET,
        SLOT;
    }
}
