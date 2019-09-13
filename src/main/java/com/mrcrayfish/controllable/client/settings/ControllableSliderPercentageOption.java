package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.gui.widget.ControllableOptionSlider;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.settings.SliderPercentageOption;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ControllableSliderPercentageOption extends SliderPercentageOption
{
    public ControllableSliderPercentageOption(String title, double minValue, double maxValue, float stepSize, Function<GameSettings, Double> getter, BiConsumer<GameSettings, Double> setter, BiFunction<GameSettings, SliderPercentageOption, String> displayNameGetter)
    {
        super(title, minValue, maxValue, stepSize, getter, setter, displayNameGetter);
    }

    @Override
    public Widget createWidget(GameSettings settings, int x, int y, int width)
    {
        return new ControllableOptionSlider(settings, x, y, width, 20, this);
    }
}
