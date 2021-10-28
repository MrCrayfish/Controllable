package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public enum Thumbstick
{
    LEFT("left"),
    RIGHT("right");

    private final String id;

    Thumbstick(String key)
    {
        this.id = key;
    }

    public String getId()
    {
        return this.id;
    }
}
