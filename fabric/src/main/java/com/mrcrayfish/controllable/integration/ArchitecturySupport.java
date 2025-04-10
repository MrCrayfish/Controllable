package com.mrcrayfish.controllable.integration;

import dev.architectury.event.events.client.ClientScreenInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Author: MrCrayfish
 */
public class ArchitecturySupport
{
    public static void sendScreenMouseReleased(Screen screen, double mouseX, double mouseY, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(ClientScreenInputEvent.MOUSE_RELEASED_PRE.invoker().mouseReleased(mc, screen, mouseX, mouseY, button).isPresent())
            return;
        if(screen.mouseReleased(mouseX, mouseY, button))
            return;
        ClientScreenInputEvent.MOUSE_RELEASED_POST.invoker().mouseReleased(mc, screen, mouseX, mouseY, button);
    }

    public static void sendScreenMouseClick(Screen screen, double mouseX, double mouseY, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(ClientScreenInputEvent.MOUSE_CLICKED_PRE.invoker().mouseClicked(mc, screen, mouseX, mouseY, button).isPresent())
            return;
        if(screen.mouseClicked(mouseX, mouseY, button))
            return;
        ClientScreenInputEvent.MOUSE_CLICKED_POST.invoker().mouseClicked(mc, screen, mouseX, mouseY, button);
    }

    public static void sendMouseDrag(Screen screen, double finalMouseX, double finalMouseY, double finalDragX, double finalDragY, int activeButton)
    {
        Minecraft mc = Minecraft.getInstance();
        if(ClientScreenInputEvent.MOUSE_DRAGGED_PRE.invoker().mouseDragged(mc, screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY).isPresent())
            return;
        if(screen.mouseDragged(finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY))
            return;
        ClientScreenInputEvent.MOUSE_DRAGGED_POST.invoker().mouseDragged(mc, screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY);
    }

    public static boolean sendScreenKeyReleased(Screen screen, int key, int scanCode, int modifiers)
    {
        Minecraft mc = Minecraft.getInstance();
        if(ClientScreenInputEvent.KEY_RELEASED_PRE.invoker().keyReleased(mc, screen, key, scanCode, modifiers).isPresent())
            return true;
        if(screen.keyReleased(key, -1, modifiers))
            return true;
        return ClientScreenInputEvent.KEY_RELEASED_POST.invoker().keyReleased(mc, screen, key, scanCode, modifiers).isPresent();
    }

    public static boolean sendScreenKeyPressed(Screen screen, int key, int scanCode, int modifiers)
    {
        Minecraft mc = Minecraft.getInstance();
        if(ClientScreenInputEvent.KEY_PRESSED_PRE.invoker().keyPressed(mc, screen, key, scanCode, modifiers).isPresent())
            return true;
        if(screen.keyPressed(key, -1, modifiers))
            return true;
        return ClientScreenInputEvent.KEY_PRESSED_POST.invoker().keyPressed(mc, screen, key, scanCode, modifiers).isPresent();
    }
}
