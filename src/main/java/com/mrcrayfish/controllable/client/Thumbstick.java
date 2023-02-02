package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum Thumbstick implements SettingEnum
{
    LEFT("controllable.thumbstick.left"),
    RIGHT("controllable.thumbstick.right");

    private final String key;

    Thumbstick(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
