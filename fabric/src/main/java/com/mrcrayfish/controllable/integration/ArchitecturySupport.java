package com.mrcrayfish.controllable.integration;

import dev.architectury.event.events.client.ClientScreenInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/**
 * Author: MrCrayfish
 */
public class ArchitecturySupport
{
    public static boolean sendScreenMouseReleased(Screen screen, double mouseX, double mouseY, int button)
    {
//        Minecraft mc = Minecraft.getInstance();
//        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
//        if(ClientScreenInputEvent.MOUSE_RELEASED_PRE.invoker().mouseReleased(mc, screen, event).isTrue())
//            return true;
//        if(screen.mouseReleased(event))
//            return true;
//        return ClientScreenInputEvent.MOUSE_RELEASED_POST.invoker().mouseReleased(mc, screen, event).isTrue();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static boolean sendScreenMouseClick(Screen screen, double mouseX, double mouseY, int button)
    {
//        Minecraft mc = Minecraft.getInstance();
//        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
//        if(ClientScreenInputEvent.MOUSE_CLICKED_PRE.invoker().mouseClicked(mc, screen, event, false).isTrue())
//            return true;
//        if(screen.mouseClicked(event, false))
//            return true;
//        return ClientScreenInputEvent.MOUSE_CLICKED_POST.invoker().mouseClicked(mc, screen, event, false).isTrue();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void sendMouseDrag(Screen screen, double finalMouseX, double finalMouseY, double finalDragX, double finalDragY, int activeButton)
    {
//        Minecraft mc = Minecraft.getInstance();
//        MouseButtonEvent event = new MouseButtonEvent(finalMouseX, finalMouseY, new MouseButtonInfo(activeButton, 0));
//        if(ClientScreenInputEvent.MOUSE_DRAGGED_PRE.invoker().mouseDragged(mc, screen, event, finalDragX, finalDragY).isPresent())
//            return;
//        if(screen.mouseDragged(event, finalDragX, finalDragY))
//            return;
//        ClientScreenInputEvent.MOUSE_DRAGGED_POST.invoker().mouseDragged(mc, screen, event, finalDragX, finalDragY);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static boolean sendScreenKeyReleased(Screen screen, int key, int scanCode, int modifiers)
    {
//        Minecraft mc = Minecraft.getInstance();
//        KeyEvent event = new KeyEvent(key, scanCode, modifiers);
//        if(ClientScreenInputEvent.KEY_RELEASED_PRE.invoker().keyReleased(mc, screen, event).isPresent())
//            return true;
//        if(screen.keyReleased(event))
//            return true;
//        return ClientScreenInputEvent.KEY_RELEASED_POST.invoker().keyReleased(mc, screen, event).isPresent();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static boolean sendScreenKeyPressed(Screen screen, int key, int scanCode, int modifiers)
    {
//        Minecraft mc = Minecraft.getInstance();
//        KeyEvent event = new KeyEvent(key, scanCode, modifiers);
//        if(ClientScreenInputEvent.KEY_PRESSED_PRE.invoker().keyPressed(mc, screen, event).isPresent())
//            return true;
//        if(screen.keyPressed(event))
//            return true;
//        return ClientScreenInputEvent.KEY_PRESSED_POST.invoker().keyPressed(mc, screen, event).isPresent();
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
