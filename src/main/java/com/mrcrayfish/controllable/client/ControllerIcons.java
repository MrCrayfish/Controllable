package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons implements SettingEnum
{
    DEFAULT("controllable.controller.default"),
    PLAYSTATION_4("controllable.controller.playstation_4"),
    PLAYSTATION_3("controllable.controller.playstation_3"),
    XBOX_ONE("controllable.controller.xbox_one"),
    XBOX_360("controllable.controller.xbox_360"),
    SWITCH_JOYCONS("controllable.controller.switch_joycons"),
    SWITCH_CONTROLLER("controllable.controller.switch_controller"),
    GAMECUBE("controllable.controller.gamecube"),
    STEAM("controllable.controller.steam");

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
