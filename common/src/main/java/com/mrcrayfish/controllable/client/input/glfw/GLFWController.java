package com.mrcrayfish.controllable.client.input.glfw;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import com.mrcrayfish.controllable.client.util.InputHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

/**
 * Author: MrCrayfish
 */
public class GLFWController extends Controller
{
    private final int deviceIndex;
    private GLFWGamepadState controller;
    private String cachedName;
    private DeviceInfo info;

    public GLFWController(int deviceIndex)
    {
        this.deviceIndex = deviceIndex;
    }

    @Override
    public boolean open()
    {
        this.controller = GLFWGamepadState.create();
        return true;
    }

    @Override
    public void close()
    {
        this.controller.close();
        this.controller = null;
    }

    @Override
    public Number getJid()
    {
        return this.deviceIndex;
    }

    @Override
    public boolean isOpen()
    {
        return this.controller != null && GLFW.glfwJoystickPresent(this.deviceIndex);
    }

    @Override
    public ButtonStates captureButtonStates()
    {
        GLFW.glfwGetGamepadState(this.deviceIndex, this.controller);
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_A));
        states.setState(Buttons.B, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_B));
        states.setState(Buttons.X, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_X));
        states.setState(Buttons.Y, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_Y));
        states.setState(Buttons.SELECT, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_BACK));
        states.setState(Buttons.HOME, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE));
        states.setState(Buttons.START, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_START));
        states.setState(Buttons.LEFT_THUMB_STICK, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB));
        states.setState(Buttons.RIGHT_THUMB_STICK, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB));
        states.setState(Buttons.LEFT_BUMPER, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER));
        states.setState(Buttons.RIGHT_BUMPER, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER));
        states.setState(Buttons.LEFT_TRIGGER, this.getLTriggerValue() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, this.getRTriggerValue() >= 0.5F);
        states.setState(Buttons.DPAD_UP, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, this.readButton(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT));
        states.setState(Buttons.LEFT_THUMB_STICK_UP, this.getLThumbStickYValue() <= -0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_DOWN, this.getLThumbStickYValue() >= 0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_LEFT, this.getLThumbStickXValue() <= -0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_RIGHT, this.getLThumbStickXValue() >= 0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_UP, this.getRThumbStickYValue() <= -0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_DOWN, this.getRThumbStickYValue() >= 0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_LEFT, this.getRThumbStickXValue() <= -0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_RIGHT, this.getRThumbStickXValue() >= 0.5F);
        return states;
    }

    private boolean readButton(int button)
    {
        return this.controller.buttons(button) == GLFW.GLFW_PRESS;
    }

    @Override
    public String getName()
    {
        if(GLFW.glfwJoystickPresent(this.deviceIndex))
        {
            if(this.cachedName == null)
            {
                this.cachedName = GLFW.glfwGetGamepadName(this.deviceIndex);
            }
            return this.cachedName;
        }
        return I18n.get("controllable.toast.controller");
    }

    @Override
    public boolean supportsRumble()
    {
        return false;
    }

    @Override
    protected boolean internalRumble(float lowFrequency, float highFrequency, int timeInMs)
    {
        return false;
    }

    @Override
    protected float internalGetLTriggerValue()
    {
        return Mth.clamp((this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1.0F) / 2.0F, 0, 1);
    }

    @Override
    protected float internalGetRTriggerValue()
    {
        return Mth.clamp((this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1.0F) / 2.0F, 0, 1);
    }

    @Override
    protected float internalGetLThumbStickXValue()
    {
        return Mth.clamp(this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X), -1, 1);
    }

    @Override
    protected float internalGetLThumbStickYValue()
    {
        return Mth.clamp(this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y), -1, 1);
    }

    @Override
    protected float internalGetRThumbStickXValue()
    {
        return Mth.clamp(this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X), -1, 1);
    }

    @Override
    protected float internalGetRThumbStickYValue()
    {
        return Mth.clamp(this.controller.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y), -1, 1);
    }

    @Override
    public DeviceInfo getInfo()
    {
        if(this.info == null)
        {
            String name = GLFW.glfwGetGamepadName(this.deviceIndex);
            String guid = GLFW.glfwGetJoystickGUID(this.deviceIndex);
            this.info = new DeviceInfo(name, guid, null, -1, -1, -1, -1, -1, -1, -1);
        }
        return this.info;
    }
}
