package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public enum Icons
{
    CONTROLLER,
    SETTINGS,
    BINDINGS,
    ADD,
    RESET,
    WORLD,
    DOWNLOAD,
    CROSS,
    SAVE,
    KEY_CAP,
    LINK,
    INFO;

    public static final ResourceLocation TEXTURE = Utils.resource("textures/gui/icons.png");
    public static final int TEXTURE_WIDTH = Icons.values().length * 11;
    public static final int TEXTURE_HEIGHT = 11;
}
