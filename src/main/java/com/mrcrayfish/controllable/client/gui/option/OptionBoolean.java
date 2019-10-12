package com.mrcrayfish.controllable.client.gui.option;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.GuiOptionButton;
import com.mrcrayfish.controllable.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class OptionBoolean extends Option<Boolean>
{
    public OptionBoolean(Supplier<Boolean> getter, Consumer<Boolean> setter, Function<Boolean, String> formatter)
    {
        super(getter, setter, formatter);
    }

    public void setValue(boolean value)
    {
        boolean oldValue = this.getGetter().get();
        this.getSetter().accept(value);
        if (oldValue != this.getGetter().get())
        {
            Controllable.getOptions().saveOptions();
        }
    }

    @Override
    public GuiButton createOption(int id, int x, int y, int width)
    {
        return new GuiOptionButton(id, x, y, width, this);
    }
}
