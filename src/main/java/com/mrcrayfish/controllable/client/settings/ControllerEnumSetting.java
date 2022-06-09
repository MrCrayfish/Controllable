package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerEnumSetting<T extends Enum<?> & SettingEnum> extends ControllerSetting<T>
{
    private final List<T> values;

    public ControllerEnumSetting(String key, Class<T> clazz, ForgeConfigSpec.ConfigValue<T> configValue)
    {
        super(key, configValue);
        this.values = Arrays.asList(clazz.getEnumConstants());
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> CycleButton.builder(T::getLabel).withValues(this.values).withTooltip(value -> this.tooltip).create(x, y, width, height, this.label, (button, value) -> {
            this.configValue.set(value);
            Config.save();
        });
    }
}
