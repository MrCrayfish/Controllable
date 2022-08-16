package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum SneakMode implements IStringSerializable
{
    TOGGLE("toggle"), HOLD("hold");

    private String id;

    SneakMode(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public String getString()
    {
        return this.id;
    }
}
