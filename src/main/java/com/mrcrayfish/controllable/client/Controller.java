package com.mrcrayfish.controllable.client;

import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerState;
import com.studiohartman.jamepad.ControllerUnpluggedException;

/**
 * A wrapper class for {@link org.lwjgl.input.Controller} to make method names more modern. Instead
 * of methods like {@link org.lwjgl.input.Controller#getXAxisValue()}, this class turns that into
 * {@link #getLThumbStickXValue()} which is much more accurate name. The class also adds in support
 * for virtual buttons, the direction pad isn't technically buttons but support for it has been
 * added within this mod, {@link #isButtonPressed(int)} adds in special cases for them.
 *
 * Author: MrCrayfish
 */
public class Controller
{
    private Mappings.Entry mapping = Mappings.DEFAULT_MAPPING;
    private ControllerIndex index;
    private ControllerState state;
    private boolean[] states;

    public Controller(ControllerIndex index)
    {
        this.index = index;
        this.states = new boolean[Buttons.LENGTH];
    }

    public int getIndex()
    {
        return index.getIndex();
    }

    public void updateState(ControllerState state)
    {
        this.state = state;
    }

    /**
     * Gets the LWJGL controller instance
     *
     * @return the lwjgl controller instance of this controller
     */
    public String getName()
    {
        if(index.isConnected())
        {
            try
            {
                return index.getName();
            }
            catch(ControllerUnpluggedException e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public float getLTriggerValue()
    {
        return state.leftTrigger > 0.05F ? state.leftTrigger : 0F;
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        return state.rightTrigger > 0.05F ? state.rightTrigger : 0F;
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        return Math.abs(state.leftStickX) > 0.05F ? state.leftStickX : 0F;
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        return Math.abs(state.leftStickY) > 0.05F ? state.leftStickY : 0F;
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        return Math.abs(state.rightStickX) > 0.05F ? state.rightStickX : 0F;
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        return Math.abs(state.rightStickY) > 0.05F ? state.rightStickY : 0F;
    }

    /**
     * Gets whether or not a button is pressed on the controller. Mappings can be found in
     * {@link Buttons} to get the index of a button.
     *
     * @param button the button to test for
     * @return if the button is pressed
     */
    public boolean isButtonPressed(int button)
    {
        return states[button];
    }

    /**
     * Sets the state of the specified button. This is whether it is pressed or not. This is an
     * internal method and should not be called.
     *
     * @param button the buttons index
     * @param state the state of the button (pressed or not)
     */
    void setButtonState(int button, boolean state)
    {
        states[button] = state;
    }

    void setMapping(Mappings.Entry mapping)
    {
        if(mapping != null)
        {
            this.mapping = mapping;
        }
    }

    Mappings.Entry getMappings()
    {
        return mapping;
    }
}
