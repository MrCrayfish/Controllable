package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum ActionVisibility implements SettingEnum
{
    ALL("controllable.actionVisibility.all"),
    MINIMAL("controllable.actionVisibility.minimal"),
    NONE("controllable.actionVisibility.none");

    private final String key;

    ActionVisibility(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
