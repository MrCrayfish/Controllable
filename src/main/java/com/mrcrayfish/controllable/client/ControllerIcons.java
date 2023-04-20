package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum ControllerIcons implements SettingEnum
{
    DEFAULT("controllable.controller.default"),
    PLAYSTATION_5("controllable.controller.playstation_5"),
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

    public static void main(String[] args)
    {
        System.out.print("\"");
        for(int i = 0; i < values().length * Buttons.LENGTH; i++)
        {
            System.out.print("\\u" + Integer.toHexString((33 + i) | 0x10000).substring(1));
            if(i > 0 && (i + 1) % Buttons.LENGTH == 0)
            {
                System.out.print("\",");
                System.out.println();
                System.out.print("\"");
            }
        }
    }
}
