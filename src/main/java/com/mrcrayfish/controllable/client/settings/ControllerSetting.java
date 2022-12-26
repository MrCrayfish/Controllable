package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class ControllerSetting<T>
{
    protected final Component label;
    @Nullable
    protected final Tooltip tooltip;
    protected final ForgeConfigSpec.ConfigValue<T> configValue;

    public ControllerSetting(String key, ForgeConfigSpec.ConfigValue<T> configValue)
    {
        this.label = Component.translatable(key);
        this.tooltip = I18n.exists(key + ".desc") ? Tooltip.create(Component.translatable(key + ".desc")) : null;
        this.configValue = configValue;
    }

    public abstract Supplier<AbstractWidget> createWidget(int x, int y, int width, int height);
}
