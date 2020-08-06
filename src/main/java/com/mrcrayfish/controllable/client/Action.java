package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public class Action
{
    private String description;
    private Side side;

    public Action(String description, Side side)
    {
        this.description = description;
        this.side = side;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
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
