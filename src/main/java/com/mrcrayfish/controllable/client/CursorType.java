package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum CursorType implements IStringSerializable
{
    LIGHT("light"),
    DARK("dark"),
    CONSOLE("console");

    String id;

    CursorType(String id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return this.id;
    }

    public static CursorType byId(String id)
    {
        for(CursorType cursorType : values())
        {
            if(cursorType.id.equals(id))
            {
                return cursorType;
            }
        }
        return LIGHT;
    }
}
