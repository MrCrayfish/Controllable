package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ScreenUtil
{
    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static Button.OnTooltip createButtonTooltip(Screen screen, Component message, int maxWidth)
    {
        return createButtonTooltip(screen, message, maxWidth, button -> button.active && button.isHoveredOrFocused());
    }

    public static Button.OnTooltip createButtonTooltip(Screen screen, Component message, int maxWidth, Predicate<Button> predicate)
    {
        return (button, poseStack, mouseX, mouseY) ->
        {
            if(predicate.test(button))
            {
                screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(message, maxWidth), mouseX, mouseY);
            }
        };
    }


}
