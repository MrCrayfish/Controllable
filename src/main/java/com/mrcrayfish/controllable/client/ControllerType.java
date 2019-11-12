package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum ControllerType implements IStringSerializable
{
    DEFAULT("default"),
    PLAYSTATION("playstation"),
    XBOX("xbox");

    String name;

    ControllerType(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    public static ControllerType byName(String name)
    {
        for(ControllerType controllerType : values())
        {
            if(controllerType.name.equals(name))
            {
                return controllerType;
            }
        }
        return DEFAULT;
    }
}
