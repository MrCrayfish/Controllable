package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class VanillaSetting<T> implements SettingProvider
{
    private final Supplier<OptionInstance<T>> optionSupplier;

    public VanillaSetting(Supplier<OptionInstance<T>> optionSupplier)
    {
        this.optionSupplier = optionSupplier;
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> this.optionSupplier.get().createButton(Minecraft.getInstance().options, x, y, width);
    }
}
