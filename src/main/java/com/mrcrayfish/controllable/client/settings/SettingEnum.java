package com.mrcrayfish.controllable.client.settings;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Author: MrCrayfish
 */
public interface SettingEnum
{
    String getKey();

    default Component getLabel()
    {
        return new TranslatableComponent(this.getKey());
    }
}