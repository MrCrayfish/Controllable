package com.mrcrayfish.controllable.client.input;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import net.minecraft.Util;

/**
 * Author: MrCrayfish
 */
public abstract class Controller
{
    protected final int deviceIndex;
    protected final ButtonStates states;
    protected long lastInputTime;

    public Controller(int deviceIndex)
    {
        this.deviceIndex = deviceIndex;
        this.states = new ButtonStates();
    }

    /**
     * Opens the controller for use. Must be closed with {@link #close} when finished.
     *
     * @return true if the controller was opened successfully
     */
    public abstract boolean open();

    /**
     * Closes the controller and can no longer be used.
     */
    public abstract void close();

    /**
     * @return The unique joystick id of this controller for the time it is connected
     */
    public abstract Number getJid();

    /**
     * @return True if this controller is open and connected
     */
    public abstract boolean isOpen();

    /**
     * Creates a new ButtonStates instance that is filled with the current states of this controller
     */
    public abstract ButtonStates captureButtonStates();

    /**
     * Gets the name of this controller. sdl2gdx prefixes the name and this method removes it.
     *
     * @return the name of this controller
     */
    public abstract String getName(); // TODO convert to component

    /**
     * Rumbles the controller if supported
     *
     * @param lowFrequency  the low frequency rumble
     * @param highFrequency the high frequency rumble
     * @param timeInMs      the time length in milliseconds
     * @return false if the controller doesn't support rumbling
     */
    public abstract boolean rumble(float lowFrequency, float highFrequency, int timeInMs);

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public abstract float getLTriggerValue();

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public abstract float getRTriggerValue();

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public abstract float getLThumbStickXValue();

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public abstract float getLThumbStickYValue();

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public abstract float getRThumbStickXValue();

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public abstract float getRThumbStickYValue();

    /**
     * Gets the device information about this controller
     *
     * @return a {@link DeviceInfo} instance
     */
    public abstract DeviceInfo getInfo();

    /**
     * @return The device index of the controller. This should not be used to determine the controller.
     */
    public int getDeviceIndex()
    {
        return this.deviceIndex;
    }

    /**
     * Used internally to update button states
     */
    public ButtonStates getTrackedButtonStates()
    {
        return this.states;
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
     * @return The time which input was last received on this controller
     */
    public final long getLastInputTime()
    {
        return this.lastInputTime;
    }

    /**
     * Updates the last input time to the current time
     */
    public final void updateInputTime()
    {
        this.lastInputTime = Util.getMillis();
    }

    /**
     * @return True if controller input has been used recently
     */
    public final boolean isBeingUsed()
    {
        return Util.getMillis() - this.lastInputTime < 4000;
    }

    /**
     * Gets the pressed value of a button. This value ranges from 0 to 1 (inclusive). If the value
     * is greater than zero, it is considered pressed. Binary buttons will only be either 0 or 1,
     * while virtual buttons like the thumbstick may be any number between 0 and 1 (inclusive).
     *
     * @param button the button to get the pressed value for
     * @return a float value between 0 and 1 (inclusive)
     */
    public final float getPressedValue(int button)
    {
        return switch(button)
        {
            case Buttons.LEFT_THUMB_STICK_UP -> {
                float value = this.getLThumbStickYValue();
                yield value < 0 ? -value : 0;
            }
            case Buttons.LEFT_THUMB_STICK_DOWN -> {
                float value = this.getLThumbStickYValue();
                yield value > 0 ? value : 0;
            }
            case Buttons.LEFT_THUMB_STICK_LEFT -> {
                float value = this.getLThumbStickXValue();
                yield value < 0 ? -value : 0;
            }
            case Buttons.LEFT_THUMB_STICK_RIGHT -> {
                float value = this.getLThumbStickXValue();
                yield value > 0 ? value : 0;
            }
            case Buttons.RIGHT_THUMB_STICK_UP -> {
                float value = this.getRThumbStickYValue();
                yield value < 0 ? -value : 0;
            }
            case Buttons.RIGHT_THUMB_STICK_DOWN -> {
                float value = this.getRThumbStickYValue();
                yield value > 0 ? value : 0;
            }
            case Buttons.RIGHT_THUMB_STICK_LEFT -> {
                float value = this.getRThumbStickXValue();
                yield value < 0 ? -value : 0;
            }
            case Buttons.RIGHT_THUMB_STICK_RIGHT -> {
                float value = this.getRThumbStickXValue();
                yield value > 0 ? value : 0;
            }
            default -> button != -1 && this.isButtonPressed(button) ? 1 : 0;
        };
    }
}
