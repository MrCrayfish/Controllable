package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.ButtonBinding;
import net.minecraft.ChatFormatting;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingData
{
    private ButtonBinding binding;
    private ChatFormatting color;

    public ButtonBindingData(ButtonBinding binding, ChatFormatting color)
    {
        this.binding = binding;
        this.color = color;
    }

    public ButtonBinding getBinding()
    {
        return binding;
    }

    public ChatFormatting getColor()
    {
        return color;
    }

    public void setColor(ChatFormatting color)
    {
        this.color = color;
    }
}
