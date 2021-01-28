package com.mrcrayfish.controllable.client;

import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ControllerManager
{
    private Set<IControllerListener> listeners = new HashSet<>();
    private Map<Integer, String> controllers = new HashMap<>();

    public void update()
    {
        this.controllers.clear();
        for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
        {
            if(GLFW.glfwJoystickIsGamepad(jid))
            {
                String controllerName = GLFW.glfwGetGamepadName(jid);
                this.controllers.put(jid, controllerName);
            }
        }
    }

    public Map<Integer, String> getControllers()
    {
        return this.controllers;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public void addControllerListener(IControllerListener listener)
    {
        this.listeners.add(listener);
    }
}
