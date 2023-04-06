package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum SprintMode implements SettingEnum
{
    TOGGLE("controllable.sprintMode.toggle"),
    ONCE("controllable.sprintMode.once");

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
