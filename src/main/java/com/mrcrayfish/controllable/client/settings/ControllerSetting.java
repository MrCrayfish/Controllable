package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class ControllerSetting<T>
{
    protected final Component label;
    protected final List<FormattedCharSequence> tooltip;
    protected final ForgeConfigSpec.ConfigValue<T> configValue;

    public ControllerSetting(String key, ForgeConfigSpec.ConfigValue<T> configValue)
    {
        this.label = Component.translatable(key);
        this.tooltip = I18n.exists(key + ".desc") ? Minecraft.getInstance().font.split(Component.translatable(key + ".desc"), 200) : Collections.emptyList();
        this.configValue = configValue;
    }

    public abstract Supplier<AbstractWidget> createWidget(int x, int y, int width, int height);
}
