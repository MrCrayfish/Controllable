package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.IEnumNext;
import com.mrcrayfish.controllable.client.gui.widget.OptionEnumWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.IStringSerializable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllableOptionEnum<T extends Enum<T> & IStringSerializable & IEnumNext<T>> extends ControllableOption<T>
{
    public ControllableOptionEnum(String titleKey, Supplier<T> getter, Consumer<T> setter, Function<T, String> formatter)
    {
        super(titleKey, getter, setter, formatter);
    }

    public void setValue(T value)
    {
        T oldValue = this.getGetter().get();
        this.getSetter().accept(value);
        if (oldValue != this.getGetter().get())
        {
            Controllable.getOptions().saveOptions();
        }
    }

    @Override
    public GuiButton createOption(int id, int x, int y, int width)
    {
        return new OptionEnumWidget<>(id, x, y, width, this);
    }
}
