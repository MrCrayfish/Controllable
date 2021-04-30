package com.mrcrayfish.controllable.client;

import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons implements IStringSerializable
{
    DEFAULT("default"),
    PLAYSTATION_4("playstation_4"),
    PLAYSTATION_3("playstation_3"),
    XBOX_ONE("xbox_one"),
    XBOX_360("xbox_360"),
    SWITCH_JOYCONS("switch_joycons"),
    SWITCH_CONTROLLER("switch_controller"),
    GAMECUBE("gamecube"),
    STEAM("steam");

    String name;

    ControllerIcons(String name)
    {
        this.name = name;
    }

    @Override
    public String getString()
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
}
