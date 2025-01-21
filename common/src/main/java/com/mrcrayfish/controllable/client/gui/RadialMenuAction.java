package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import net.minecraft.ChatFormatting;

/**
 * Author: MrCrayfish
 */
public class RadialMenuAction
{
    private final ButtonBinding binding;
    private ChatFormatting color;

    public RadialMenuAction(ButtonBinding binding, ChatFormatting color)
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
