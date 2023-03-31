package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.framework.api.config.EnumProperty;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerEnumSetting<T extends Enum<T> & SettingEnum> extends ControllerSetting<T>
{
    private final List<T> values;

    public ControllerEnumSetting(String key, Class<T> clazz, EnumProperty<T> configValue)
    {
        super(key, configValue);
        this.values = Arrays.asList(clazz.getEnumConstants());
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> {
            AbstractWidget widget = CycleButton.builder(T::getLabel).withInitialValue(this.configValue.get()).withValues(this.values).withTooltip(value -> this.tooltip).create(x, y, width, height, this.label, (button, value) -> {
                this.configValue.set(value);
            });
            widget.setTooltipDelay(500);
            return widget;
        };
    }
}
