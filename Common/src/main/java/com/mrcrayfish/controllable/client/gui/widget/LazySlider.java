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

    public LazySlider(int x, int y, int width, int height, Component label, double initialValue, double minValue, double maxValue, double step, Consumer<Double> onChange)
    {
        super(x, y, width, height, label, parseValue(initialValue, step));
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.onChange = onChange;
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
        this.setMessage(Component.empty().append(this.label).append(": ").append(FORMATTER.format(this.getValue())));
    }

    @Override
    protected void applyValue() {}

    private static double parseValue(double value, double step)
    {
        double scale = 1.0 / step;
        return Mth.clamp(Mth.floor(value * scale) / scale, 0.0, 1.0);
    }
}
