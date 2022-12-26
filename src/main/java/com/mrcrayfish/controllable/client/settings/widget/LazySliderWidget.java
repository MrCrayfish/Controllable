package com.mrcrayfish.controllable.client.settings.widget;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.function.Consumer;

/**
 * A custom slider that only sends change updates when the mouse is released
 *
 * Author: MrCrayfish
 */
public class LazySliderWidget extends ForgeSlider
{
    private final Consumer<Double> onChange;
    private boolean pressed = false;

    public LazySliderWidget(Component label, int x, int y, int width, int height, double minValue, double maxValue, double currentValue, double stepSize, Consumer<Double> onChange)
    {
        super(x, y, width, height, label, CommonComponents.EMPTY, minValue, maxValue, currentValue, stepSize, 1, true);
        this.onChange = onChange;
    }

    @Override
    protected void updateMessage()
    {
        this.setMessage(Component.empty().append(this.prefix).append(": ").append(this.getValueString()));
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
