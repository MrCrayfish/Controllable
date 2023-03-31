package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class ScreenUtil
{
    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static Button button(int x, int y, int width, int height, Component label, Button.OnPress onPress)
    {
        return Button.builder(label, onPress).pos(x, y).size(width, height).build();
    }
}
