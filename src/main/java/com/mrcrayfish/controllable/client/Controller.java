package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.resources.I18n;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;

import javax.annotation.Nullable;

import static org.libsdl.SDL.*;

/**
 *  A wrapper class that aims to reduce the exposure to the underlying controller library. This class
 *  provides simple and straight forward methods to retrieve values about the current state of the
 *  controller.
 */
public class Controller
{
    private int jid;
    private String cachedName;
    private Mappings.Entry mapping;
    private SDL2Controller controller;
    private ButtonStates states;

    public Controller(int jid)
    {
        this.jid = jid;
        this.controller = Controllable.getManager().getSDL2ControllerById(jid);
        if(this.controller == null) throw new IllegalArgumentException("Illegal controller jid: " + jid);
        this.states = new ButtonStates();
        this.getName(); //cache the name straight away
    }

    public int getJid()
    {
        return this.jid;
    }

    /**
     * Gets the underlying {@link SDL2Controller} of this this controller instance.
     * This is gives you direct access to the controller state.
     *
     * @return the sdl2controller controller instance
     */
    public SDL2Controller getSDL2Controller()
    {
        return controller;
    }

    /**
     * Used internally to update button states
     */
    public ButtonStates getButtonsStates()
    {
        return states;
    }

    /**
     * Gets the name of this controller. sdl2gdx prefixes the name and this method removes it.
     *
     * @return the name of this controller
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
     * Gets whether the specified button is pressed or not. It is recommended to use
     * {@link ButtonBinding} instead as this method is a raw approach.
     *
     * @param button the button to check
     *
     * @return if the specified button is pressed or not
     */
    public boolean isButtonPressed(int button)
    {
        return this.states.getState(button);
    }

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public float getLTriggerValue()
    {
        return (controller.getAxis(SDL_CONTROLLER_AXIS_TRIGGERLEFT) + 1.0F) / 2.0F;
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        return (this.controller.getAxis(SDL_CONTROLLER_AXIS_TRIGGERRIGHT) + 1.0F) / 2.0F;
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        int axis = this.isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_RIGHTX : SDL_CONTROLLER_AXIS_LEFTX;
        return this.controller.getAxis(axis) * (this.isFlipLeftX() ? -1 : 1);
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        int axis = this.isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_RIGHTY : SDL_CONTROLLER_AXIS_LEFTY;
        return this.controller.getAxis(axis) * (this.isFlipLeftY() ? -1 : 1);
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        int axis = this.isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_LEFTX : SDL_CONTROLLER_AXIS_RIGHTX;
        return this.controller.getAxis(axis) * (this.isFlipRightX() ? -1 : 1);
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        int axis = this.isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_LEFTY : SDL_CONTROLLER_AXIS_RIGHTY;
        return this.controller.getAxis(axis) * (this.isFlipRightY() ? -1 : 1);
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
        return this.mapping;
    }

    private boolean isThumbsticksSwitched()
    {
        return this.mapping != null && this.mapping.isThumbsticksSwitched();
    }

    public boolean isFlipLeftX()
    {
        return this.mapping != null && this.mapping.isFlipLeftX();
    }

    public boolean isFlipLeftY()
    {
        return this.mapping != null && this.mapping.isFlipLeftY();
    }

    public boolean isFlipRightX()
    {
        return this.mapping != null && this.mapping.isFlipRightX();
    }

    public boolean isFlipRightY()
    {
        return this.mapping != null && this.mapping.isFlipRightY();
    }
}