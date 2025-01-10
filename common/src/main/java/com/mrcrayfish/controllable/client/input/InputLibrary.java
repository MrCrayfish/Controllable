package com.mrcrayfish.controllable.client.input;

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

    private final Supplier<AdaptiveControllerManager> managerFactory;

    InputLibrary(Supplier<AdaptiveControllerManager> managerFactory)
    {
        this.managerFactory = managerFactory;
    }

    public AdaptiveControllerManager createManager()
    {
        return this.managerFactory.get();
    }
}
