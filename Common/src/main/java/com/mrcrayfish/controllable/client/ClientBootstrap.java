package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class ClientBootstrap
{
    public static void init()
    {
        InputProcessor.get();
        RadialMenuHandler.instance();
        Controllable.init();
        ControllerEvents.init();
        RenderEvents.init();

        /* Attempts to load the first controller connected if auto select is enabled */
        if(Config.CLIENT.client.options.autoSelect.get())
        {
            if(GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1) && GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1))
            {
                InputProcessor.get().setController(new Controller(GLFW.GLFW_JOYSTICK_1));
            }
        }
    }
}
