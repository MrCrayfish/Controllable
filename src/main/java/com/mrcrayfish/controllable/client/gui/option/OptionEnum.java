package com.mrcrayfish.controllable.client.gui.option;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.IEnumNext;
import com.mrcrayfish.controllable.client.gui.GuiOptionEnum;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.IStringSerializable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class OptionEnum<T extends Enum<T> & IStringSerializable & IEnumNext<T>> extends Option<T>
{
    public OptionEnum(Supplier<T> getter, Consumer<T> setter, Function<T, String> formatter)
    {
        super(getter, setter, formatter);
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
        return new GuiOptionEnum<>(id, x, y, width, this);
    }
}
