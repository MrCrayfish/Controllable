package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.settings.widget.LazySliderWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerSliderSetting extends ControllerSetting<Double>
{
    private final double min;
    private final double max;
    private final double stepSize;

    public ControllerSliderSetting(String key, ForgeConfigSpec.ConfigValue<Double> configValue, double min, double max, double stepSize)
    {
        super(key, configValue);
        this.min = min;
        this.max = max;
        this.stepSize = stepSize;
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> new LazySliderWidget(this.label, x, y, width, height, this.min, this.max, this.configValue.get(), this.stepSize, value -> {
            this.configValue.set(value);
            Config.save();
        }).setTooltip(this.tooltip);
    }
}
