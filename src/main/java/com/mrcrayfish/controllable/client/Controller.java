package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.ButtonStates;
import net.minecraft.client.resources.language.I18n;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import javax.annotation.Nullable;

/**
 *  A wrapper class that aims to reduce the exposure to the underlying controller library. This class
 *  provides simple and straight forward methods to retrieve values about the current state of the
 *  controller.
 */
public class Controller
{
    private final int jid;
    private GLFWGamepadState controller;
    private ButtonStates states;
    private String cachedName;
    private Mappings.Entry mapping;

    public Controller(int jid)
    {
        this.jid = jid;
        this.controller = GLFWGamepadState.create();
        this.states = new ButtonStates();
        this.getName(); //cache the name straight away
    }

    public int getJid()
    {
        return this.jid;
    }

    /**
     * Gets the underlying {@link GLFWGamepadState} of this this controller instance.
     * This is gives you direct access to the controller state.
     *
     * @return the sdl2controller controller instance
     */
    public GLFWGamepadState getGamepadState()
    {
        return this.controller;
    }

    /**
     * Updates the state of the gamepad.
     *
     * @return True if successfully updated otherwise controller is disconnected
     */
    public boolean updateGamepadState()
    {
        return GLFW.glfwGetGamepadState(this.jid, this.controller);
    }

    /**
     * Used internally to update button states
     */
    public ButtonStates getButtonsStates()
    {
        return this.states;
    }

    /**
     * Gets the name of this controller. sdl2gdx prefixes the name and this method removes it.
     *
     * @return the name of this controller
     */
    public String getName()
    {
        if(GLFW.glfwJoystickPresent(this.jid))
        {
            if(this.cachedName == null)
            {
                this.cachedName = GLFW.glfwGetGamepadName(this.jid);
            }
            return this.cachedName;
        }
        return I18n.get("controllable.toast.controller");
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
        return (this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1.0F) / 2.0F;
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        return (this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1.0F) / 2.0F;
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        int axis = this.isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X : GLFW.GLFW_GAMEPAD_AXIS_LEFT_X;
        return this.controller.axes(axis) * (this.isFlipLeftX() ? -1 : 1);
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        int axis = this.isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y : GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y;
        return this.controller.axes(axis) * (this.isFlipLeftY() ? -1 : 1);
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        int axis = this.isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_LEFT_X : GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X;
        return this.controller.axes(axis) * (this.isFlipRightX() ? -1 : 1);
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        int axis = this.isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y : GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y ;
        return this.controller.axes(axis) * (this.isFlipRightY() ? -1 : 1);
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
