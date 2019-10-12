package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.gui.option.OptionBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/**
 * Author: MrCrayfish
 */
public class GuiOptionButton extends GuiButton
{
    private OptionBoolean option;

    public GuiOptionButton(int buttonId, int x, int y, int width, OptionBoolean option)
    {
        super(buttonId, x, y, width, 20, option.getFormatter().apply(option.getGetter().get()));
        this.option = option;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if(super.mousePressed(mc, mouseX, mouseY))
        {
            this.option.setValue(!this.option.getGetter().get());
            this.displayString = this.option.getFormatter().apply(this.option.getGetter().get());
            return true;
        }
        return false;
    }
}
