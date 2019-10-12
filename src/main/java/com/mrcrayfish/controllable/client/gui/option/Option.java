package com.mrcrayfish.controllable.client.gui.option;

import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class Option<T>
{
    private Supplier<T> getter;
    private Consumer<T> setter;
    private Function<T, String> formatter;

    public Option(Supplier<T> getter, Consumer<T> setter, Function<T, String> formatter)
    {
        this.getter = getter;
        this.setter = setter;
        this.formatter = formatter;
    }

    public abstract GuiButton createOption(int id, int x, int y, int width);

    public Supplier<T> getGetter()
    {
        return getter;
    }

    public Consumer<T> getSetter()
    {
        return setter;
    }

    public Function<T, String> getFormatter()
    {
        return formatter;
    }
}
