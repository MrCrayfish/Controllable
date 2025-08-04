package com.mrcrayfish.controllable.client.input;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.settings.Thumbstick;
import com.mrcrayfish.controllable.client.util.InputHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public abstract class Controller
{
    protected final ButtonStates states;
    protected long lastInputTime;

    public Controller()
    {
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
     * @return True if this controller supports rumble
     */
    public abstract boolean supportsRumble();

    /**
     * Rumbles the controller if supported
     *
     * @param lowFrequency  the low frequency rumble
     * @param highFrequency the high frequency rumble
     * @param timeInMs      the time length in milliseconds
     * @return false if the controller doesn't support rumbling
     */
    public final boolean rumble(float lowFrequency, float highFrequency, int timeInMs)
    {
        return this.isAccessible() && this.internalRumble(lowFrequency, highFrequency, timeInMs);
    }

    protected abstract boolean internalRumble(float lowFrequency, float highFrequency, int timeInMs);

    /**
     * Gets the value of the left trigger
     *
     * @return the left trigger value
     */
    public final float getLTriggerValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetLTriggerValue(), this.getTriggerDeadzone()) : 0;
    }

    protected abstract float internalGetLTriggerValue();

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public final float getRTriggerValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetRTriggerValue(), this.getTriggerDeadzone()) : 0;
    }

    protected abstract float internalGetRTriggerValue();

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public final float getLThumbStickXValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetLThumbStickXValue(), this.getThumbstickDeadzone(Thumbstick.LEFT)) : 0;
    }

    protected abstract float internalGetLThumbStickXValue();

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public final float getLThumbStickYValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetLThumbStickYValue(), this.getThumbstickDeadzone(Thumbstick.LEFT)) : 0;
    }

    protected abstract float internalGetLThumbStickYValue();

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public final float getRThumbStickXValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetRThumbStickXValue(), this.getThumbstickDeadzone(Thumbstick.RIGHT)) : 0;
    }

    protected abstract float internalGetRThumbStickXValue();

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public final float getRThumbStickYValue()
    {
        return this.isAccessible() ? InputHelper.applyDeadzone(this.internalGetRThumbStickYValue(), this.getThumbstickDeadzone(Thumbstick.RIGHT)) : 0;
    }

    protected abstract float internalGetRThumbStickYValue();

    /**
     * Gets the device information about this controller
     *
     * @return a {@link DeviceInfo} instance
     */
    public abstract DeviceInfo getInfo();

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
        return this.isAccessible() && this.states.getState(button);
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
     * @return True if controller input has been used recently
     */
    public final boolean isUsingVirtualCursor()
    {
        return this.isBeingUsed() && Controllable.getCursor().getMode().isController() && Controllable.getCursor().isEnabled();
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

    /**
     * @return True if the controller is accessible for interaction (reading inputs, rumble, etc)
     */
    public final boolean isAccessible()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.isWindowActive() || Config.CLIENT.options.backgroundInput.get();
    }

    /**
     * @return The deadzone value for triggers
     */
    protected float getTriggerDeadzone()
    {
        return Config.CLIENT.options.triggerDeadZone.get().floatValue();
    }

    /**
     * @return The deadzone value for the thumbsticks
     */
    @Deprecated
    protected float getThumbstickDeadzone()
    {
        return Config.CLIENT.options.thumbstickDeadZone.get().floatValue();
    }

    /**
     * @return The deadzone value for a thumbstick
     */
    protected float getThumbstickDeadzone(Thumbstick thumbstick)
    {
        if(Config.CLIENT.options.advanced.advancedMode.get())
        {
            return switch(thumbstick)
            {
                case LEFT -> Config.CLIENT.options.advanced.leftThumbstickDeadZone.get().floatValue();
                case RIGHT -> Config.CLIENT.options.advanced.rightThumbstickDeadZone.get().floatValue();
            };
        }
        return Config.CLIENT.options.thumbstickDeadZone.get().floatValue();
    }
}
