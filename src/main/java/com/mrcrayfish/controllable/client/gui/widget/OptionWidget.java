package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.client.settings.ControllableOptionBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Author: MrCrayfish
 */
public class OptionWidget extends GuiButton
{
    private ControllableOptionBoolean option;

    public OptionWidget(int buttonId, int x, int y, int width, ControllableOptionBoolean option)
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
