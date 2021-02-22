package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllableOptionSlider extends ControllableOption<Double>
{
    private float stepSize;
    private float minValue;
    private float maxValue;

    public ControllableOptionSlider(String titleKey, float stepSize, float minValue, float maxValue, Supplier<Double> getter, Consumer<Double> setter, Function<Double, String> formatter)
    {
        super(titleKey, getter, setter, formatter);
        this.stepSize = stepSize;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public void setValue(float value)
    {
        double oldValue = this.getGetter().get();
        this.getSetter().accept(this.denormalize(value));
        if (oldValue != this.getGetter().get())
        {
            Controllable.getOptions().saveOptions();
        }
    }

    public float normalize()
    {
        return MathHelper.clamp((this.snapToStepClamp(this.getGetter().get().floatValue()) - this.minValue) / (this.maxValue - this.minValue), 0.0F, 1.0F);
    }

    public double denormalize(float value)
    {
        return this.snapToStepClamp(this.minValue + (this.maxValue - this.minValue) * MathHelper.clamp(value, 0.0F, 1.0F));
    }

    public float snapToStepClamp(float value)
    {
        return MathHelper.clamp(this.snapToStep(value), this.minValue, this.maxValue);
    }

    private float snapToStep(float value)
    {
        if(this.stepSize > 0.0F)
        {
            value = this.stepSize * (float) Math.round(value / this.stepSize);
        }
        return value;
    }

    @Override
    public GuiButton createOption(int id, int x, int y, int width)
    {
        return new OptionSliderWidget(id, x, y, width, this);
    }
}
