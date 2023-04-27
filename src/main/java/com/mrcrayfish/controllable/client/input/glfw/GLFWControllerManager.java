package com.mrcrayfish.controllable.client.input.glfw;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Mac will still use GLFW due to LibSDL4J requiring JVM arguments to load correctly.
 *
 * Author: MrCrayfish
 */
public class GLFWControllerManager extends ControllerManager
{
    @Override
    public void init() {}

    @Override
    public void dispose() {}

    @Override
    public Controller createController(int deviceIndex, Number jid)
    {
        return new GLFWController(jid.intValue());
    }

    @Nullable
    @Override
    public Controller connectToFirstGameController()
    {
        if(Config.CLIENT.options.autoSelect.get())
        {
            for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
            {
                if(GLFW.glfwJoystickPresent(jid) && GLFW.glfwJoystickIsGamepad(jid))
                {
                    GLFWController controller = new GLFWController(jid);
                    if(this.setActiveController(controller))
                    {
                        return controller;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void updateMappings(InputStream is) throws IOException
    {
        if(is != null)
        {
            byte[] bytes = ByteStreams.toByteArray(is);
            ByteBuffer buffer = MemoryUtil.memASCIISafe(new String(bytes));
            if(buffer != null && GLFW.glfwUpdateGamepadMappings(buffer))
            {
                Constants.LOG.info("Successfully updated gamepad mappings");
                return;
            }
        }
        Constants.LOG.info("No gamepad mappings were updated");
    }

    @Override
    protected int getRawControllerCount()
    {
        int connectedCount = 0;
        for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
        {
            if(GLFW.glfwJoystickIsGamepad(jid))
            {
                connectedCount++;
            }
        }
        return connectedCount;
    }

    @Override
    protected Map<Number, Pair<Integer, String>> createRawControllerMap()
    {
        Map<Number, Pair<Integer, String>> controllers = new HashMap<>();
        for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++)
        {
            if(GLFW.glfwJoystickIsGamepad(jid))
            {
                String controllerName = GLFW.glfwGetGamepadName(jid);
                controllers.put(jid, Pair.of(jid, controllerName));
            }
        }
        return controllers;
    }
}
