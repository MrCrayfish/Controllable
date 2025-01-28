package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.framework.api.config.AbstractProperty;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class ControllerSetting<T> implements SettingProvider
{
    protected final Component label;
    @Nullable
    protected final Tooltip tooltip;
    protected final AbstractProperty<T> configValue;

    public ControllerSetting(String key, AbstractProperty<T> configValue)
    {
        this.label = Component.translatable(key);
        this.tooltip = I18n.exists(key + ".desc") ? Tooltip.create(Component.translatable(key + ".desc")) : null;
        this.configValue = configValue;
    }
}
