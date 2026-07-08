package com.mrcrayfish.controllable.client.util;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Author: MrCrayfish
 */
public class MouseHooks
{
    private static long previousTime;
    private static int previousButton;

    /**
     * @return The scaled x position of the mouse. If virtual cursor is disabled, it will use the native cursor x position.
     */
    private static double getScreenMouseX()
    {
        if(Controllable.getCursor().isEnabled())
        {
            return Controllable.getCursor().getScreenX();
        }
        Window window = Minecraft.getInstance().getWindow();
        return Minecraft.getInstance().mouseHandler.getScaledXPos(window);
    }

    /**
     * @return The scaled y position of the mouse. If virtual cursor is disabled, it will use the native cursor y position.
     */
    private static double getScreenMouseY()
    {
        if(Controllable.getCursor().isEnabled())
        {
            return Controllable.getCursor().getScreenY();
        }
        Window window = Minecraft.getInstance().getWindow();
        return Minecraft.getInstance().mouseHandler.getScaledYPos(window);
    }

    public static void sendMouseClickEvent(Screen screen, int button)
    {
        if(screen != null)
        {
            sendMouseClickEvent(screen, button, getScreenMouseX(), getScreenMouseY());
        }
    }

    public static void sendMouseClickEventWithShift(Screen screen, int button)
    {
        if(screen != null)
        {
            var info = new MouseButtonInfo(button, InputConstants.MOD_SHIFT);
            var event = new MouseButtonEvent(getScreenMouseX(), getScreenMouseY(), info);
            sendMouseClickEvent(screen, event);
        }
    }

    public static void sendMouseClickEvent(Screen screen, int button, double mouseX, double mouseY)
    {
        var info = new MouseButtonInfo(button, 0);
        var event = new MouseButtonEvent(mouseX, mouseY, info);
        sendMouseClickEvent(screen, event);
    }

    public static void sendMouseClickEvent(Screen screen, MouseButtonEvent event)
    {
        /*if(screen instanceof AbstractContainerScreen && Controllable.isEmiLoaded())
        {
            if(EmiSupport.invokeMouseClick(button, screenCursorX, screenCursorY))
            {
                return;
            }
        }*/
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButtonInfo(event.buttonInfo());
            ClientServices.CLIENT.setLastMouseEventTime(Blaze3D.getTime());

            // 1.21.9 introduced double click
            long currentTime = Util.getMillis();
            boolean doubleClick = currentTime - previousTime < 250L && previousButton == event.button();
            if(ClientServices.CLIENT.sendScreenMouseClick(screen, event, doubleClick))
            {
                previousTime = currentTime;
                previousButton = event.button();
            }
        }
    }

    public static void sendMouseReleasedEvent(Screen screen, int button)
    {
        sendMouseReleasedEvent(screen, button, getScreenMouseX(), getScreenMouseY());
    }

    public static void sendMouseReleasedEventWithShift(Screen screen, int button)
    {
        var info = new MouseButtonInfo(button, InputConstants.MOD_SHIFT);
        var event = new MouseButtonEvent(getScreenMouseX(), getScreenMouseY(), info);
        sendMouseReleasedEvent(screen, event);
    }

    public static void sendMouseReleasedEvent(Screen screen, int button, double mouseX, double mouseY)
    {
        var info = new MouseButtonInfo(button, 0);
        var event = new MouseButtonEvent(mouseX, mouseY, info);
        sendMouseReleasedEvent(screen, event);
    }

    public static void sendMouseReleasedEvent(Screen screen, MouseButtonEvent event)
    {
        /*if(screen instanceof AbstractContainerScreen && Controllable.isEmiLoaded())
        {
            if(EmiSupport.invokeMouseReleased(button, screenCursorX, screenCursorY))
            {
                return;
            }
        }*/
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButtonInfo(null);
            ClientServices.CLIENT.sendScreenMouseReleased(screen, event);
        }
    }

    public static void invokeMouseMoved(Screen screen, double cursorX, double cursorY, double deltaX, double deltaY)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null && mc.gui.overlay() == null)
        {
            // Send mouse moved event to screen
            double screenCursorX = cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double screenCursorY = cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            screen.mouseMoved(screenCursorX, screenCursorY);

            // Invoke a mouse drag if possible
            MouseButtonInfo activeInfo = ClientServices.CLIENT.getActiveMouseButtonInfo();
            double lastMouseEventTime = ClientServices.CLIENT.getLastMouseEventTime();
            if(activeInfo != null && lastMouseEventTime > 0)
            {
                if(screen instanceof AbstractContainerScreen<?>)
                {
                    if(Controllable.isEmiLoaded())
                    {
                        /*if(EmiSupport.invokeMouseDragged(activeMouseButton, screenCursorX, screenCursorY, deltaX, deltaY))
                        {
                            return;
                        }*/
                    }
                }
                ClientServices.CLIENT.sendMouseDrag(screen, deltaX, deltaY, screenCursorX, screenCursorY, activeInfo.button());
            }
        }
    }
}
