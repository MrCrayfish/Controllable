package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.input.glfw.GLFWControllerManager;
import com.mrcrayfish.controllable.client.input.sdl2.SDL2ControllerManager;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public enum InputLibrary
{
    GLFW(GLFWControllerManager::new),
    SDL2(SDL2ControllerManager::new); // Default

    private final Supplier<ControllerManager> managerFactory;

    InputLibrary(Supplier<ControllerManager> managerFactory)
    {
        this.managerFactory = managerFactory;
    }

    public ControllerManager createManager()
    {
        return this.managerFactory.get();
    }
}