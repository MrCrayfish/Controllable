package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum ActionVisibility implements SettingEnum
{
    ALL("controllable.action_visibility.all"),
    MINIMAL("controllable.action_visibility.minimal"),
    NONE("controllable.action_visibility.none");

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
