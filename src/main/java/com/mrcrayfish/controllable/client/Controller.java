package com.mrcrayfish.controllable.client;

import net.minecraft.client.resources.I18n;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;

import javax.annotation.Nullable;

import static org.libsdl.SDL.*;

public class Controller
{
    private String cachedName;
    private Mappings.Entry mapping;
    private SDL2Controller controller;

    public Controller(SDL2Controller controller)
    {
        this.controller = controller;
        this.getName(); //cache the name straight away
    }

    public SDL2Controller getNativeController()
    {
        return controller;
    }

    /**
     * Gets the LWJGL controller instance
     *
     * @return the lwjgl controller instance of this controller
     */
    public String getName()
    {
        if(this.controller.isConnected())
        {
            if(this.cachedName == null)
            {
                this.cachedName = this.controller.getName().replace("SDL GameController ", "").replace("SDL Joystick ", "");
            }
            return this.cachedName;
        }
        return I18n.format("controllable.toast.controller");
    }

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public float getLTriggerValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_TRIGGERLEFT);
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_TRIGGERRIGHT);
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_LEFTX);
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_LEFTY);
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_RIGHTX);
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        return controller.getAxis(SDL_CONTROLLER_AXIS_RIGHTY);
    }

    /**
     * Sets the mapping for this controller
     *
     * @param mapping the mapping to assign
     */
    public void setMapping(Mappings.Entry mapping)
    {
        this.mapping = mapping;
    }

    /**
     * Gets the mapping of this controller
     *
     * @return the mapping of this controller or null if not present
     */
    @Nullable
    public Mappings.Entry getMapping()
    {
        return mapping;
    }
}