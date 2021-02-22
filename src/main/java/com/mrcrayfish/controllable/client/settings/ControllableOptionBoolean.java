package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.IToolTip;
import com.mrcrayfish.controllable.client.gui.widget.OptionWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllableOptionBoolean extends ControllableOption<Boolean> implements IToolTip
{
    public ControllableOptionBoolean(String titleKey, Supplier<Boolean> getter, Consumer<Boolean> setter, Function<Boolean, String> formatter)
    {
        super(titleKey, getter, setter, formatter);
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
        return new OptionWidget(id, x, y, width, this);
    }
}
