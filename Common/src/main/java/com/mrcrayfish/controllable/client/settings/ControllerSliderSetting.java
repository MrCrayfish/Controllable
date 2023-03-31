package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.framework.api.config.DoubleProperty;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;

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
            /*AbstractWidget slider = new LazySliderWidget(this.label, x, y, width, height, this.min, this.max, this.configValue.get(), this.stepSize, value -> {
                this.configValue.set(value);
            });
            slider.setTooltip(this.tooltip);
            slider.setTooltipDelay(500);
            return slider;*/
            return Button.builder(this.label, var1 -> {}).pos(x, y).size(width, height).build();
        };
    }
}
