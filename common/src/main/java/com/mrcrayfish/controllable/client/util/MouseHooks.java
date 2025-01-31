package com.mrcrayfish.controllable.client.util;

import com.mojang.blaze3d.Blaze3D;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Author: MrCrayfish
 */
public class MouseHooks
{
    /**
     * Invokes a mouse click in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    public static void invokeMouseClick(Screen screen, int button)
    {
        if(screen != null)
        {
            int screenCursorX = Controllable.getCursor().getScreenX();
            int screenCursorY = Controllable.getCursor().getScreenY();
            invokeMouseClick(screen, button, screenCursorX, screenCursorY);
        }
    }

    /**
     * Invokes a mouse click in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     * @param cursorScreenX the x position of the cursor in screen space
     * @param cursorScreenY the y position of the cursor in screen space
     */
    public static void invokeMouseClick(Screen screen, int button, double cursorScreenX, double cursorScreenY)
    {
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(button);
            ClientServices.CLIENT.setLastMouseEventTime(Blaze3D.getTime());
            ClientServices.CLIENT.sendScreenMouseClickPre(screen, cursorScreenX, cursorScreenY, button);
        }
    }

    /**
     * Invokes a mouse released in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    public static void invokeMouseReleased(Screen screen, int button)
    {
        if(screen != null)
        {
            int screenCursorX = Controllable.getCursor().getScreenX();
            int screenCursorY = Controllable.getCursor().getScreenY();
            invokeMouseReleased(screen, button, screenCursorX, screenCursorY);
        }
    }

    /**
     * Invokes a mouse released in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     * @param cursorScreenX the x position of the cursor in screen space
     * @param cursorScreenY the y position of the cursor in screen space
     */
    public static void invokeMouseReleased(Screen screen, int button, double cursorScreenX, double cursorScreenY)
    {
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(-1);
            ClientServices.CLIENT.sendScreenMouseReleasedPre(screen, cursorScreenX, cursorScreenY, button);
        }
    }

    public static void invokeMouseMoved(Screen screen, double cursorX, double cursorY, double deltaX, double deltaY)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null && mc.getOverlay() == null)
        {
            // Send mouse moved event to screen
            double screenCursorX = cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double screenCursorY = cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            Screen.wrapScreenError(() -> {
                screen.mouseMoved(screenCursorX, screenCursorY);
            }, "Controllable mouseMoved event handler", screen.getClass().getCanonicalName());

            // Invoke a mouse drag if possible
            int activeMouseButton = ClientServices.CLIENT.getActiveMouseButton();
            double lastMouseEventTime = ClientServices.CLIENT.getLastMouseEventTime();
            if(activeMouseButton != -1 && lastMouseEventTime > 0)
            {
                ClientServices.CLIENT.sendMouseDrag(screen, deltaX, deltaY, screenCursorX, screenCursorY, activeMouseButton);
            }
        }
    }
}
