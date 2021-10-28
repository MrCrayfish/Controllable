package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons
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

    private final String id;

    ControllerIcons(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public static ControllerIcons byName(String name)
    {
        for(ControllerIcons controllerIcons : values())
        {
            if(controllerIcons.id.equals(name))
            {
                return controllerIcons;
            }
        }
        return DEFAULT;
    }
}
