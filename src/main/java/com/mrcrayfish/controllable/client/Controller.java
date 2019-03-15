package com.mrcrayfish.controllable.client;

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
    private org.lwjgl.input.Controller controller;
    private boolean[] states;

    public Controller(org.lwjgl.input.Controller controller)
    {
        this.controller = controller;
        this.states = new boolean[controller.getButtonCount() + 4]; //The extra 4 is for dpad buttons
    }

    /**
     * Gets the LWJGL controller instance
     *
     * @return the lwjgl controller instance of this controller
     */
    public org.lwjgl.input.Controller getRawController()
    {
        return controller;
    }

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public float getLTriggerValue()
    {
        return (controller.getRXAxisValue() + 1.0F) / 2F;
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        return (controller.getRYAxisValue() + 1.0F) / 2F;
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        return controller.getXAxisValue();
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        return controller.getYAxisValue();
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        return controller.getZAxisValue();
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        return controller.getRZAxisValue();
    }

    /**
     * Gets the directional pad x value
     *
     * @return the directional pad x value
     */
    public float getDpadXValue()
    {
        return controller.getPovX();
    }

    /**
     * Gets the directional pad y value
     *
     * @return the directional pad y value
     */
    public float getDpadYValue()
    {
        return controller.getPovY();
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
}
