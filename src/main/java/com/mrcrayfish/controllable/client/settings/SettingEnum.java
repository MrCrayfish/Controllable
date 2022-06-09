package com.mrcrayfish.controllable.client.settings;

import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public interface SettingEnum
{
    String getKey();

    default Component getLabel()
    {
        return Component.translatable(this.getKey());
    }
}
