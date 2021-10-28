package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ControllableProgressOption extends ProgressOption implements IToolTip
{
    private TranslatableComponent toolTip;

    public ControllableProgressOption(String key, double minValue, double maxValue, float step, Function<Options, Double> getter, BiConsumer<Options, Double> setter, BiFunction<Options, ProgressOption, Component> nameGetter)
    {
        super(key, minValue, maxValue, step, getter, setter, nameGetter);
        this.toolTip = new TranslatableComponent(key + ".desc");
    }

    @Override
    public List<FormattedCharSequence> getToolTip()
    {
        return Minecraft.getInstance().font.split(this.toolTip, 200);
    }
}
