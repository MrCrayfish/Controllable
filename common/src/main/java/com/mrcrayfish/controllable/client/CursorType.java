package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.settings.SettingEnum;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public enum CursorType implements SettingEnum
{
    LIGHT("controllable.cursor.light", ItemHeldBehaviour.HIDE, true),
    DARK("controllable.cursor.dark", ItemHeldBehaviour.HIDE, true),
    CONSOLE("controllable.cursor.console", ItemHeldBehaviour.SHOW, false),
    CONSOLE_PLUS("controllable.cursor.console_plus", ItemHeldBehaviour.HIDE, true),
    LEGACY_LIGHT("controllable.cursor.legacy_light", ItemHeldBehaviour.HIDE, true),
    LEGACY_DARK("controllable.cursor.legacy_dark", ItemHeldBehaviour.HIDE, true);

    public static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/cursor.png");

    private final String key;
    private final ItemHeldBehaviour behaviour;
    private final boolean scaleHover;

    CursorType(String key, ItemHeldBehaviour behaviour, boolean scaleHover)
    {
        this.key = key;
        this.behaviour = behaviour;
        this.scaleHover = scaleHover;
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

    public boolean isScaleHover()
    {
        return this.scaleHover;
    }
}
