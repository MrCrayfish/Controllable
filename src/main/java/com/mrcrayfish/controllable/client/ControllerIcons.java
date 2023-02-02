package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons implements SettingEnum
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

    private final String key;

    ControllerIcons(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
