package com.mrcrayfish.controllable.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class LazySlider extends AbstractSliderButton
{
    private static final DecimalFormat FORMATTER = new DecimalFormat("###0.###");

    private final Component label;
    private final double minValue;
    private final double maxValue;
    private final double step;
    private final Consumer<Double> onChange;
    private boolean pressed = false;
    private boolean valueOnly = false;

    public LazySlider(int x, int y, int width, int height, Component label, double initialValue, double minValue, double maxValue, double step, Consumer<Double> onChange)
    {
        super(x, y, width, height, label, parseValue(initialValue, step, minValue, maxValue));
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.onChange = onChange;
        this.updateMessage();
    }

    public void valueOnly()
    {
        this.valueOnly = true;
        this.updateMessage();
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        this.pressed = true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.isValidClickButton(button) && this.pressed)
        {
            this.onChange.accept(this.getValue());
            this.pressed = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public double getValue()
    {
        double scale = 1.0 / this.step;
        double value = this.minValue + (this.maxValue - this.minValue) * this.value;
        return Mth.floor(value * scale) / scale;
    }

    @Override
    protected void updateMessage()
    {
        if(this.valueOnly)
        {
            this.setMessage(Component.empty().append(FORMATTER.format(this.getValue())));
        }
        else
        {
            this.setMessage(Component.empty().append(this.label).append(": ").append(FORMATTER.format(this.getValue())));
        }
    }

    @Override
    protected void applyValue() {}

    private static double parseValue(double value, double step, double minValue, double maxValue)
    {
        double range = Math.abs(maxValue - minValue);
        value -= minValue;
        value /= range;
        return value;
    }

    public void stepForward()
    {
        if(this.value < 1.0)
        {
            this.value = Mth.clamp(this.value + this.step / (this.maxValue - this.minValue), 0.0, 1.0);
            this.updateMessage();
        }
    }

    public void stepBackward()
    {
        if(this.value > 0.0)
        {
            this.value = Mth.clamp(this.value - this.step / (this.maxValue - this.minValue), 0.0, 1.0);
            this.updateMessage();
        }
    }

    public void triggerChangeCallback()
    {
        this.onChange.accept(this.getValue());
    }
}
