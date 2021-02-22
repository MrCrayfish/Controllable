package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons implements IStringSerializable, IEnumNext<ControllerIcons>
{
    DEFAULT("default"),
    PLAYSTATION("playstation"),
    XBOX("xbox");

    String name;

    ControllerIcons(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    public static ControllerIcons byName(String name)
    {
        for(ControllerIcons controllerIcons : values())
        {
            if(controllerIcons.name.equals(name))
            {
                return controllerIcons;
            }
        }
        return DEFAULT;
    }

    @Override
    public ControllerIcons next()
    {
        return values()[(ordinal() + 1) % values().length];
    }
}
