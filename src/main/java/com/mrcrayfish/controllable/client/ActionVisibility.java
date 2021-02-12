package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum ActionVisibility implements IStringSerializable
{
    ALL("all"),
    MINIMAL("minimal"),
    NONE("none");

    String name;

    ActionVisibility(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}
