package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public enum ActionVisibility
{
    ALL("all"),
    MINIMAL("minimal"),
    NONE("none");

    private final String id;

    ActionVisibility(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }
}
