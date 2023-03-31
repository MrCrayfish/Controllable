package com.mrcrayfish.controllable.client;

import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class Action
{
    private Component description;
    private Side side;

    public Action(Component description, Side side)
    {
        this.description = description;
        this.side = side;
    }

    public Component getDescription()
    {
        return this.description;
    }

    public void setDescription(Component description)
    {
        this.description = description;
    }

    public Side getSide()
    {
        return this.side;
    }

    public void setSide(Side side)
    {
        this.side = side;
    }

    public enum Side
    {
        LEFT, RIGHT;
    }
}
