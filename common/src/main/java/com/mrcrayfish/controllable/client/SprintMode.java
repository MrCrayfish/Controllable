package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum SprintMode implements SettingEnum
{
    TOGGLE("controllable.sprint_mode.toggle"),
    ONCE("controllable.sprint_mode.once");

    private final String key;

    SprintMode(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
