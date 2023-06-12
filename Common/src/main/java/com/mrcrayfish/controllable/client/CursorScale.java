package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum CursorScale implements SettingEnum
{
    SMALL("controllable.cursor_scale.small"),
    LARGE("controllable.cursor_scale.large");

    private final String key;

    CursorScale(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
