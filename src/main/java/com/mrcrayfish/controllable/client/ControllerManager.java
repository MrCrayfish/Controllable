package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ControllerManager
{
    private Set<IControllerListener> listeners = new HashSet<>();
    private Map<Integer, String> controllers = new HashMap<>();

    public void update()
    {
        Preconditions.checkState(Minecraft.getInstance().isOnExecutionThread(),
                "update must be called from the main thread");
        int connectedCount = 0;
        for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
        {
            if(GLFW.glfwJoystickIsGamepad(jid))
            {
                connectedCount++;
            }
        }

        if(connectedCount == this.controllers.size())
            return;

        Map<Integer, String> oldControllers = this.controllers;
        Map<Integer, String> newControllers = new HashMap<>();
        for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
        {
            if(GLFW.glfwJoystickIsGamepad(jid))
            {
                String controllerName = GLFW.glfwGetGamepadName(jid);
                newControllers.put(jid, controllerName);
            }
        }

        this.controllers = newControllers;

        newControllers.forEach((jid, name) ->
        {
            if(!oldControllers.containsKey(jid))
            {
                this.listeners.forEach(listener -> listener.connected(jid));
            }
        });

        oldControllers.forEach((jid, name) ->
        {
            if(!newControllers.containsKey(jid))
            {
                this.listeners.forEach(listener -> listener.disconnected(jid));
            }
        });
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
