package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

/**
 * Author: MrCrayfish
 */
public class RadialMenuAction
{
    private final ButtonBinding binding;
    private TextColor color;

    public RadialMenuAction(ButtonBinding binding, TextColor color)
    {
        this.binding = binding;
        this.color = color;
    }

    public ButtonBinding getBinding()
    {
        return binding;
    }

    public TextColor getColor()
    {
        return color;
    }

    public void setColor(TextColor color)
    {
        this.color = color;
    }
}
