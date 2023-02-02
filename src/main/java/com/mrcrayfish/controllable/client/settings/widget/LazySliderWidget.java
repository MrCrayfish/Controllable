package com.mrcrayfish.controllable.client.settings.widget;

import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A custom slider that only sends change updates when the mouse is released
 *
 * Author: MrCrayfish
 */
public class LazySliderWidget extends ForgeSlider implements TooltipAccessor
{
    private List<FormattedCharSequence> tooltip = Collections.emptyList();
    private final Consumer<Double> onChange;
    private boolean pressed = false;

    public LazySliderWidget(Component label, int x, int y, int width, int height, double minValue, double maxValue, double currentValue, double stepSize, Consumer<Double> onChange)
    {
        super(x, y, width, height, label, TextComponent.EMPTY, minValue, maxValue, currentValue, stepSize, 1, true);
        this.onChange = onChange;
    }

    public LazySliderWidget setTooltip(List<FormattedCharSequence> tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    protected void updateMessage()
    {
        this.setMessage(new TextComponent("").append(this.prefix).append(": ").append(this.getValueString()));
    }

    @Override
    public List<FormattedCharSequence> getTooltip()
    {
        return this.tooltip;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        this.pressed = true;
    }

    // Only send change when releasing mouse to avoid lots of calls to save config
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
}