package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.framework.api.config.BoolProperty;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerToggleSetting extends ControllerSetting<Boolean>
{
    public ControllerToggleSetting(String key, BoolProperty configValue)
    {
        super(key, configValue);
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> {
            AbstractWidget widget = CycleButton.onOffBuilder(this.configValue.get()).withTooltip(value -> this.tooltip).create(x, y, width, height, this.label, (button, value) -> {
                this.configValue.set(value);
            });
            widget.setTooltipDelay(500);
            return widget;
        };
    }
}
