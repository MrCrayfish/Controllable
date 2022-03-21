package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ControllableProgressOption extends ProgressOption
{
    public ControllableProgressOption(String key, double minValue, double maxValue, float step, Function<Options, Double> getter, BiConsumer<Options, Double> setter, BiFunction<Options, ProgressOption, Component> nameGetter)
    {
        super(key, minValue, maxValue, step, getter, setter, nameGetter, minecraft ->
                Minecraft.getInstance().font.split(new TranslatableComponent(key + ".desc"), 200));
    }
}
