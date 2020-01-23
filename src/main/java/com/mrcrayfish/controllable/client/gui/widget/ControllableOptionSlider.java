package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.widget.OptionSlider;
import net.minecraft.client.settings.SliderPercentageOption;

/**
 * Author: MrCrayfish
 */
public class ControllableOptionSlider extends OptionSlider
{
    private final SliderPercentageOption option;

    public ControllableOptionSlider(GameSettings settings, int x, int y, int width, int height, SliderPercentageOption option)
    {
        super(settings, x, y, width, height, option);
        this.option = option;
    }

    @Override
    protected void applyValue()
    {
        this.option.set(this.options, this.option.denormalizeValue(this.value));
        Controllable.getOptions().saveOptions();
    }
}
