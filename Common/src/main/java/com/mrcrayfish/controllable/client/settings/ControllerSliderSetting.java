package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.gui.widget.LazySlider;
import com.mrcrayfish.framework.api.config.DoubleProperty;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerSliderSetting extends ControllerSetting<Double>
{
    private final double min;
    private final double max;
    private final double stepSize;

    public ControllerSliderSetting(String key, DoubleProperty property, double min, double max, double stepSize)
    {
        super(key, property);
        this.min = min;
        this.max = max;
        this.stepSize = stepSize;
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> {
            AbstractWidget slider = new LazySlider(x, y, width, height, this.label, this.configValue.get(), this.min, this.max, this.stepSize, this.configValue::set);
            slider.setTooltip(this.tooltip);
            slider.setTooltipDelay(500);
            return slider;
        };
    }
}
