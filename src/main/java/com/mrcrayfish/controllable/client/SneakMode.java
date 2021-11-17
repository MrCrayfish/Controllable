package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public enum SneakMode
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
}
