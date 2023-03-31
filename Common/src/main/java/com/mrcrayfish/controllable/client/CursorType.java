package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.settings.SettingEnum;

/**
 * Author: MrCrayfish
 */
public enum CursorType implements SettingEnum
{
    LIGHT("controllable.cursor.light"),
    DARK("controllable.cursor.dark"),
    CONSOLE("controllable.cursor.console"),
    LEGACY_LIGHT("controllable.cursor.legacy_light"),
    LEGACY_DARK("controllable.cursor.legacy_dark");

    private final String key;

    CursorType(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
