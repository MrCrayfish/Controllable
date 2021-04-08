package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraft.util.text.TextFormatting;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingData
{
    private ButtonBinding binding;
    private TextFormatting color;

    public ButtonBindingData(ButtonBinding binding, TextFormatting color)
    {
        this.binding = binding;
        this.color = color;
    }

    public ButtonBinding getBinding()
    {
        return binding;
    }

    public TextFormatting getColor()
    {
        return color;
    }

    public void setColor(TextFormatting color)
    {
        this.color = color;
    }
}
