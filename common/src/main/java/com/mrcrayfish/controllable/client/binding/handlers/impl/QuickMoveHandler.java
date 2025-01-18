package com.mrcrayfish.controllable.client.binding.handlers.impl;

import com.mrcrayfish.controllable.client.binding.handlers.OnPressHandler;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.util.MouseHooks;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class QuickMoveHandler extends OnPressHandler
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return context.screen().map(screen -> () -> {
            context.player().ifPresent(player -> {
                if(player.inventoryMenu.getCarried().isEmpty()) {
                    MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                } else {
                    MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                }
            });
        });
    }
}
