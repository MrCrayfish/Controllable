package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import javax.annotation.Nullable;

import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton.*;
import static io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis.*;
import static io.github.libsdl4j.api.joystick.SdlJoystickConst.*;

/**
 *  A wrapper class that aims to reduce the exposure to the underlying controller library. This class
 *  provides simple and straight forward methods to retrieve values about the current state of the
 *  controller.
 */
public class Controller
{
    private final SDL_GameController controller;
    private final int jid;
    private final byte[] rawStates;
    private final ButtonStates states;
    private String cachedName;
    private Mappings.Entry mapping;

    public Controller(int jid)
    {
        this.controller = SDL_GameControllerOpen(jid);
        this.jid = jid;
        this.rawStates = new byte[SDL_CONTROLLER_BUTTON_MAX];
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
    public byte[] getGamepadState()
    {
        return this.rawStates;
    }

    /**
     * Updates the state of the gamepad.
     *
     * @return True if successfully updated otherwise controller is disconnected
     */
    public boolean updateGamepadState()
    {
        SDL_GameControllerUpdate();
        this.readButton(SDL_CONTROLLER_BUTTON_A);
        this.readButton(SDL_CONTROLLER_BUTTON_B);
        this.readButton(SDL_CONTROLLER_BUTTON_X);
        this.readButton(SDL_CONTROLLER_BUTTON_Y);
        this.readButton(SDL_CONTROLLER_BUTTON_BACK);
        this.readButton(SDL_CONTROLLER_BUTTON_GUIDE);
        this.readButton(SDL_CONTROLLER_BUTTON_START);
        this.readButton(SDL_CONTROLLER_BUTTON_LEFTSTICK);
        this.readButton(SDL_CONTROLLER_BUTTON_RIGHTSTICK);
        this.readButton(SDL_CONTROLLER_BUTTON_LEFTSHOULDER);
        this.readButton(SDL_CONTROLLER_BUTTON_RIGHTSHOULDER);
        this.readButton(SDL_CONTROLLER_BUTTON_DPAD_UP);
        this.readButton(SDL_CONTROLLER_BUTTON_DPAD_DOWN);
        this.readButton(SDL_CONTROLLER_BUTTON_DPAD_LEFT);
        this.readButton(SDL_CONTROLLER_BUTTON_DPAD_RIGHT);
        this.readButton(SDL_CONTROLLER_BUTTON_MISC1);
        this.readButton(SDL_CONTROLLER_BUTTON_PADDLE1);
        this.readButton(SDL_CONTROLLER_BUTTON_PADDLE2);
        this.readButton(SDL_CONTROLLER_BUTTON_PADDLE3);
        this.readButton(SDL_CONTROLLER_BUTTON_PADDLE4);
        this.readButton(SDL_CONTROLLER_BUTTON_TOUCHPAD);
        return SDL_GameControllerGetAttached(this.controller);
    }

    private void readButton(int button)
    {
        this.rawStates[button] = SDL_GameControllerGetButton(this.controller, button);
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

    public void dispose()
    {
        SDL_GameControllerClose(this.controller);
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
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_TRIGGERLEFT) / (float) SDL_JOYSTICK_AXIS_MAX, 0, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.triggerDeadZone.get().floatValue());
    }

    /**
     * Gets the value of the right trigger
     *
     * @return the right trigger value
     */
    public float getRTriggerValue()
    {
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_TRIGGERRIGHT) / (float) SDL_JOYSTICK_AXIS_MAX, 0, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.triggerDeadZone.get().floatValue());
    }

    /**
     * Gets the left thumb stick x value
     *
     * @return the left thumb stick x value
     */
    public float getLThumbStickXValue()
    {
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_LEFTX) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.thumbstickDeadZone.get().floatValue());
    }

    /**
     * Gets the left thumb stick y value
     *
     * @return the left thumb stick y value
     */
    public float getLThumbStickYValue()
    {
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_LEFTY) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.thumbstickDeadZone.get().floatValue());
    }

    /**
     * Gets the right thumb stick x value
     *
     * @return the right thumb stick x value
     */
    public float getRThumbStickXValue()
    {
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_RIGHTX) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.thumbstickDeadZone.get().floatValue());
    }

    /**
     * Gets the right thumb stick y value
     *
     * @return the right thumb stick y value
     */
    public float getRThumbStickYValue()
    {
        float input = Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_RIGHTY) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
        return ClientHelper.applyDeadzone(input, Config.CLIENT.client.options.thumbstickDeadZone.get().floatValue());
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
