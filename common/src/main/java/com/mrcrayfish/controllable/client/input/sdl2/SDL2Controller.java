package com.mrcrayfish.controllable.client.input.sdl2;

import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import com.mrcrayfish.controllable_sdl.api.gamecontroller.SDL_GameController;
import com.mrcrayfish.controllable_sdl.api.joystick.SDL_Joystick;
import com.mrcrayfish.controllable_sdl.api.joystick.SDL_JoystickID;
import com.sun.jna.Memory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import static com.mrcrayfish.controllable_sdl.api.gamecontroller.SDL_GameControllerAxis.*;
import static com.mrcrayfish.controllable_sdl.api.gamecontroller.SDL_GameControllerButton.*;
import static com.mrcrayfish.controllable_sdl.api.gamecontroller.SdlGamecontroller.*;
import static com.mrcrayfish.controllable_sdl.api.joystick.SdlJoystick.*;
import static com.mrcrayfish.controllable_sdl.api.joystick.SdlJoystickConst.SDL_JOYSTICK_AXIS_MAX;
import static com.mrcrayfish.controllable_sdl.api.sensor.SDL_SensorType.SDL_SENSOR_GYRO;
import static com.mrcrayfish.controllable_sdl.api.sensor.SdlSensor.SDL_SensorUpdate;

/**
 *  A wrapper class that aims to reduce the exposure to the underlying controller library. This class
 *  provides simple and straight forward methods to retrieve values about the current state of the
 *  controller.
 */
public class SDL2Controller extends Controller
{
    private final int deviceIndex;
    private final SDL_JoystickID jid;
    private SDL_GameController controller;
    private String cachedName;
    private DeviceInfo info;
    private boolean gyro;

    public SDL2Controller(int deviceIndex)
    {
        this.jid = SDL_JoystickGetDeviceInstanceID(deviceIndex);
        this.deviceIndex = deviceIndex;
        this.getName(); //cache the name straight away
    }

    @Override
    public SDL_JoystickID getJid()
    {
        return this.jid;
    }

    @Override
    public boolean open()
    {
        updateControllerState();
        if(this.controller == null)
        {
            this.controller = SDL_GameControllerOpen(this.deviceIndex);
            this.updateGyroState();
            return this.controller != null;
        }
        return true;
    }

    @Override
    public void close()
    {
        if(this.controller != null)
        {
            SDL_GameControllerClose(this.controller);
            this.controller = null;
        }
    }

    @Override
    public boolean isOpen()
    {
        updateControllerState();
        return this.controller != null && SDL_GameControllerGetAttached(this.controller);
    }

    @Override
    public ButtonStates captureButtonStates()
    {
        SDL_SensorUpdate();
        SDL_GameControllerUpdate();
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, this.readButton(SDL_CONTROLLER_BUTTON_A));
        states.setState(Buttons.B, this.readButton(SDL_CONTROLLER_BUTTON_B));
        states.setState(Buttons.X, this.readButton(SDL_CONTROLLER_BUTTON_X));
        states.setState(Buttons.Y, this.readButton(SDL_CONTROLLER_BUTTON_Y));
        states.setState(Buttons.SELECT, this.readButton(SDL_CONTROLLER_BUTTON_BACK));
        states.setState(Buttons.HOME, this.readButton(SDL_CONTROLLER_BUTTON_GUIDE));
        states.setState(Buttons.START, this.readButton(SDL_CONTROLLER_BUTTON_START));
        states.setState(Buttons.LEFT_THUMB_STICK, this.readButton(SDL_CONTROLLER_BUTTON_LEFTSTICK));
        states.setState(Buttons.RIGHT_THUMB_STICK, this.readButton(SDL_CONTROLLER_BUTTON_RIGHTSTICK));
        states.setState(Buttons.LEFT_BUMPER, this.readButton(SDL_CONTROLLER_BUTTON_LEFTSHOULDER));
        states.setState(Buttons.RIGHT_BUMPER, this.readButton(SDL_CONTROLLER_BUTTON_RIGHTSHOULDER));
        states.setState(Buttons.LEFT_TRIGGER, this.getLTriggerValue() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, this.getRTriggerValue() >= 0.5F);
        states.setState(Buttons.DPAD_UP, this.readButton(SDL_CONTROLLER_BUTTON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, this.readButton(SDL_CONTROLLER_BUTTON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, this.readButton(SDL_CONTROLLER_BUTTON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, this.readButton(SDL_CONTROLLER_BUTTON_DPAD_RIGHT));
        states.setState(Buttons.MISC, this.readButton(SDL_CONTROLLER_BUTTON_MISC1));
        states.setState(Buttons.PADDLE_ONE, this.readButton(SDL_CONTROLLER_BUTTON_PADDLE1));
        states.setState(Buttons.PADDLE_TWO, this.readButton(SDL_CONTROLLER_BUTTON_PADDLE2));
        states.setState(Buttons.PADDLE_THREE, this.readButton(SDL_CONTROLLER_BUTTON_PADDLE3));
        states.setState(Buttons.PADDLE_FOUR, this.readButton(SDL_CONTROLLER_BUTTON_PADDLE4));
        states.setState(Buttons.TOUCHPAD, this.readButton(SDL_CONTROLLER_BUTTON_TOUCHPAD));
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

    @SuppressWarnings("MagicConstant")
    private boolean readButton(int button)
    {
        return SDL_GameControllerGetButton(this.controller, button) == 1;
    }

    @Override
    public String getName()
    {
        if(SDL_IsGameController(this.deviceIndex))
        {
            if(this.cachedName == null)
            {
                this.cachedName = SDL_GameControllerNameForIndex(this.deviceIndex);
            }
            return this.cachedName;
        }
        return I18n.get("controllable.toast.controller");
    }

    @Override
    public boolean supportsRumble()
    {
        return this.controller != null && SDL_GameControllerHasRumble(this.controller);
    }

    @Override
    protected boolean internalRumble(float lowFrequency, float highFrequency, int timeInMs)
    {
        if(this.controller == null)
            return false;
        lowFrequency = Mth.clamp(lowFrequency, 0.0F, 1.0F);
        highFrequency = Mth.clamp(highFrequency, 0.0F, 1.0F);
        return SDL_GameControllerRumble(this.controller, (short) (0xFFFF * lowFrequency), (short) (0xFFFF * highFrequency), timeInMs) == 0;
    }

    @Override
    protected float internalGetLTriggerValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_TRIGGERLEFT) / (float) SDL_JOYSTICK_AXIS_MAX, 0, 1);
    }

    @Override
    protected float internalGetRTriggerValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_TRIGGERRIGHT) / (float) SDL_JOYSTICK_AXIS_MAX, 0, 1);
    }

    @Override
    protected float internalGetLThumbStickXValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_LEFTX) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
    }

    @Override
    protected float internalGetLThumbStickYValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_LEFTY) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
    }

    @Override
    protected float internalGetRThumbStickXValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_RIGHTX) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
    }

    @Override
    protected float internalGetRThumbStickYValue()
    {
        return Mth.clamp(SDL_GameControllerGetAxis(this.controller, SDL_CONTROLLER_AXIS_RIGHTY) / (float) SDL_JOYSTICK_AXIS_MAX, -1, 1);
    }

    @Override
    public DeviceInfo getInfo()
    {
        if(this.info == null)
        {
            String name = SDL_GameControllerName(this.controller);
            SDL_Joystick joystick = SDL_GameControllerGetJoystick(this.controller);
            String guid = SDL_JoystickGetGUID(joystick).toString();
            String serial = SDL_GameControllerGetSerial(this.controller);
            int type = SDL_GameControllerGetType(this.controller);
            short vendor = SDL_GameControllerGetVendor(this.controller);
            short product = SDL_GameControllerGetProduct(this.controller);
            short productVersion = SDL_GameControllerGetProductVersion(this.controller);
            short firmware = SDL_GameControllerGetFirmwareVersion(this.controller);
            int buttons = SDL_JoystickNumButtons(joystick);
            int axes = SDL_JoystickNumAxes(joystick);
            this.info = new DeviceInfo(name, guid, serial, type, vendor, product, productVersion, firmware, buttons, axes);
        }
        return this.info;
    }

    private void updateGyroState()
    {
        this.gyro = false;
        if(this.controller == null)
            return;
        if(!SDL_GameControllerHasSensor(this.controller, SDL_SENSOR_GYRO))
            return;
        if(SDL_GameControllerSetSensorEnabled(this.controller, SDL_SENSOR_GYRO, true) != 0)
            return;
        this.gyro = true;
    }

    private static void updateControllerState()
    {
        SDL_GameControllerUpdate();
        SDL_JoystickUpdate();
    }

    @Override
    public boolean supportsGyroscope()
    {
        return this.gyro;
    }

    @Override
    public Vector3f getGyroscope()
    {
        int num = 3;
        byte[] bytes = new byte[Float.BYTES * num];
        try(Memory memory = new Memory(bytes.length))
        {
            SDL_GameControllerGetSensorData(this.controller, SDL_SENSOR_GYRO, memory, num);
            float[] values = memory.getFloatArray(0, num);
            return new Vector3f(values[0], values[1], values[2]);
        }
    }
}
