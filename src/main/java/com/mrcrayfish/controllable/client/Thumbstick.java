package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum Thumbstick implements IStringSerializable
{
    LEFT("controllable.thumbstick.left"),
    RIGHT("controllable.thumbstick.right");

    String key;

    Thumbstick(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return this.key;
    }

    @Override
    public String getString()
    {
        return this.key;
    }
}
