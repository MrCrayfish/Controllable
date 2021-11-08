package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public enum CursorType
{
    LIGHT("light"),
    DARK("dark"),
    CONSOLE("console"),
    LEGACY_LIGHT("legacy_light"),
    LEGACY_DARK("legacy_dark");

    private final String id;

    CursorType(String id)
    {
        this.id = id;
    }

    public String getId()
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
