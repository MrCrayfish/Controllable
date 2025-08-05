package com.mrcrayfish.controllable.client.util;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.Window;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.integration.EmiSupport;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class MouseHooks
{
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
        double x = Minecraft.getInstance().mouseHandler.xpos();
        return x * window.getGuiScaledWidth() / window.getScreenWidth();
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
        // TODO TEST
        Window window = Minecraft.getInstance().getWindow();
        double y = Minecraft.getInstance().mouseHandler.ypos();
        return y * window.getGuiScaledHeight() / window.getScreenHeight();
    }

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
            double screenCursorX = getScreenMouseX();
            double screenCursorY = getScreenMouseY();
            if(screen instanceof AbstractContainerScreen && Controllable.isEmiLoaded())
            {
                if(EmiSupport.invokeMouseClick(button, screenCursorX, screenCursorY))
                {
                    return;
                }
            }
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
            ClientServices.CLIENT.sendScreenMouseClick(screen, cursorScreenX, cursorScreenY, button);
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
            double screenCursorX = getScreenMouseX();
            double screenCursorY = getScreenMouseY();
            if(screen instanceof AbstractContainerScreen && Controllable.isEmiLoaded())
            {
                if(EmiSupport.invokeMouseReleased(button, screenCursorX, screenCursorY))
                {
                    return;
                }
            }
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
            ClientServices.CLIENT.sendScreenMouseReleased(screen, cursorScreenX, cursorScreenY, button);
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
                if(screen instanceof AbstractContainerScreen<?>)
                {
                    if(Controllable.isEmiLoaded())
                    {
                        if(EmiSupport.invokeMouseDragged(activeMouseButton, screenCursorX, screenCursorY, deltaX, deltaY))
                        {
                            return;
                        }
                    }
                }
                ClientServices.CLIENT.sendMouseDrag(screen, deltaX, deltaY, screenCursorX, screenCursorY, activeMouseButton);
            }
        }
    }
}
