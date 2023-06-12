package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.settings.SettingEnum;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public enum CursorType implements SettingEnum
{
    LIGHT("controllable.cursor.light", ItemHeldBehaviour.HIDE),
    DARK("controllable.cursor.dark", ItemHeldBehaviour.HIDE),
    CONSOLE("controllable.cursor.console", ItemHeldBehaviour.SHOW),
    CONSOLE_PLUS("controllable.cursor.console_plus", ItemHeldBehaviour.HIDE),
    LEGACY_LIGHT("controllable.cursor.legacy_light", ItemHeldBehaviour.HIDE),
    LEGACY_DARK("controllable.cursor.legacy_dark", ItemHeldBehaviour.HIDE);

    public static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/cursor.png");

    private final String key;
    private final ItemHeldBehaviour behaviour;

    CursorType(String key, ItemHeldBehaviour behaviour)
    {
        this.key = key;
        this.behaviour = behaviour;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }

    public ItemHeldBehaviour getBehaviour()
    {
        return this.behaviour;
    }
}
