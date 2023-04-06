package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum SneakMode implements SettingEnum
{
    TOGGLE("controllable.sneakMode.toggle"),
    HOLD("controllable.sneakMode.hold");

    private final String key;

    SneakMode(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
