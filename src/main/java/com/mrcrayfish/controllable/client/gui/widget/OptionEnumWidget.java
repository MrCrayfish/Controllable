package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.client.IEnumNext;
import com.mrcrayfish.controllable.client.settings.ControllableOptionEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.IStringSerializable;

/**
 * Author: MrCrayfish
 */
public class OptionEnumWidget<T extends Enum<T> & IStringSerializable & IEnumNext<T>> extends GuiButton
{
    private ControllableOptionEnum<T> option;

    public OptionEnumWidget(int buttonId, int x, int y, int width, ControllableOptionEnum<T> option)
    {
        super(buttonId, x, y, width, 20, option.getFormatter().apply(option.getGetter().get()));
        this.option = option;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if(super.mousePressed(mc, mouseX, mouseY))
        {
            this.option.setValue(this.option.getGetter().get().next());
            this.displayString = this.option.getFormatter().apply(this.option.getGetter().get());
            return true;
        }
        return false;
    }
}
