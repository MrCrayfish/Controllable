package com.mrcrayfish.controllable.client;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Author: MrCrayfish
 */
public class Action implements Comparable<Action>
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

    @Override
    public int compareTo(@NotNull Action o)
    {
        int result = this.side.compareTo(o.side);
        if(result == 0)
        {
            return o.description.getString().compareTo(o.description.getString());
        }
        return result;
    }

    public enum Side
    {
        LEFT, RIGHT;
    }
}
