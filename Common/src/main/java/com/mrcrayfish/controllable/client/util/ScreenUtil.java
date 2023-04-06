package com.mrcrayfish.controllable.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public static Button button(int x, int y, int width, int height, Component label, Button.OnPress onPress)
    {
        return Button.builder(label, onPress).pos(x, y).size(width, height).build();
    }

    public static void drawOutlinedBox(PoseStack poseStack, int x, int y, int width, int height, int color)
    {
        GuiComponent.fill(poseStack, x, y, x + width, y + 1, color);                          // Top
        GuiComponent.fill(poseStack, x, y + 1, x + 1, y + height - 1, color);                 // Left
        GuiComponent.fill(poseStack, x, y + height - 1, x + width, y + height, color);        // Bottom
        GuiComponent.fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color); // Right
    }

    public static void drawRoundedBox(PoseStack poseStack, int x, int y, int width, int height, int backgroundColor)
    {
        Minecraft mc = Minecraft.getInstance();
        Screen.fill(poseStack, x - 3 + 1, y, x + width + 2 - 1, y + 1, backgroundColor);
        Screen.fill(poseStack, x - 3, y + 1, x + width + 2, y + height - 1, backgroundColor);
        Screen.fill(poseStack, x - 3 + 1, y + height - 1, x + width + 2 - 1, y + height, backgroundColor);
    }

    public static Optional<GuiEventListener> findHoveredListener(Screen screen, double mouseX, double mouseY, Predicate<GuiEventListener> condition)
    {
        return findHoveredListener(screen.children(), mouseX, mouseY, condition);
    }

    private static Optional<GuiEventListener> findHoveredListener(List<? extends GuiEventListener> listeners, double mouseX, double mouseY, Predicate<GuiEventListener> condition)
    {
        for(GuiEventListener listener : listeners)
        {
            if(condition.test(listener) && listener.isMouseOver(mouseX, mouseY))
            {
                return Optional.of(listener);
            }
            else if(listener instanceof TabButton button && button.isSelected())
            {
                List<AbstractWidget> children = new ArrayList<>();
                button.tab().visitChildren(children::add);
                return findHoveredListener(children, mouseX, mouseY, condition);
            }
            else if(listener instanceof ContainerEventHandler handler)
            {
                return findHoveredListener(handler.children(), mouseX, mouseY, condition);
            }
        }
        return Optional.empty();
    }
}
