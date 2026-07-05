package com.mrcrayfish.controllable.client.input.steam;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.input.ButtonStates;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import com.mrcrayfish.controllable.client.input.RelativePointerController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.hid4java.HidDevice;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;

public class SteamController extends Controller implements RelativePointerController
{
    private static final int REPORT_SIZE = 64;
    private static final int FEATURE_REPORT_SIZE = 64;
    private static final int MAX_REPORTS_PER_POLL = 64;
    private static final long LIZARD_HEARTBEAT_MS = 800;

    private static final int ID_CLEAR_DIGITAL_MAPPINGS = 0x81;
    private static final int ID_LOAD_DEFAULT_SETTINGS = 0x82;
    private static final int ID_SET_DEFAULT_DIGITAL_MAPPINGS = 0x80;
    private static final int ID_SET_SETTINGS_VALUES = 0x87;
    private static final int SETTING_LEFT_TRACKPAD_MODE = 7;
    private static final int SETTING_RIGHT_TRACKPAD_MODE = 8;
    private static final int SETTING_LIZARD_MODE = 9;
    private static final int SETTING_SMOOTH_ABSOLUTE_MOUSE = 27;
    private static final int SETTING_WIRELESS_PACKET_VERSION = 49;
    private static final int TRACKPAD_NONE = 7;

    private static final int ID_TRITON_CONTROLLER_STATE = 0x42;
    private static final int ID_TRITON_CONTROLLER_STATE_BLE = 0x45;
    private static final int ID_TRITON_CONTROLLER_STATE_TIMESTAMP = 0x47;

    private static final int TRITON_A = 0x00000001;
    private static final int TRITON_B = 0x00000002;
    private static final int TRITON_X = 0x00000004;
    private static final int TRITON_Y = 0x00000008;
    private static final int TRITON_QAM = 0x00000010;
    private static final int TRITON_R3 = 0x00000020;
    private static final int TRITON_VIEW = 0x00000040;
    private static final int TRITON_R4 = 0x00000080;
    private static final int TRITON_R5 = 0x00000100;
    private static final int TRITON_R = 0x00000200;
    private static final int TRITON_DPAD_DOWN = 0x00000400;
    private static final int TRITON_DPAD_RIGHT = 0x00000800;
    private static final int TRITON_DPAD_LEFT = 0x00001000;
    private static final int TRITON_DPAD_UP = 0x00002000;
    private static final int TRITON_MENU = 0x00004000;
    private static final int TRITON_L3 = 0x00008000;
    private static final int TRITON_STEAM = 0x00010000;
    private static final int TRITON_L4 = 0x00020000;
    private static final int TRITON_L5 = 0x00040000;
    private static final int TRITON_L = 0x00080000;
    private static final int TRITON_RIGHT_PAD_TOUCH = 0x00200000;
    private static final int TRITON_RIGHT_PAD_CLICK = 0x00400000;
    private static final int TRITON_RIGHT_TRIGGER_CLICK = 0x00800000;
    private static final int TRITON_LEFT_PAD_CLICK = 0x04000000;
    private static final int TRITON_LEFT_TRIGGER_CLICK = 0x08000000;

    private static final float INT16_MAX = 32767.0F;
    private static final float GYRO_SCALE = (float) (2000.0 * Math.PI / 180.0 / 32768.0);
    private static final float TRACKPAD_MOUSE_SCALE = 1.0F / 24.0F;
    private static final float TRACKPAD_MOUSE_ACCELERATION = 0.25F;

    private final SteamControllerDevice deviceInfo;
    private final DeviceInfo info;
    private HidDevice device;
    private Snapshot snapshot = Snapshot.EMPTY;
    private long lastLizardHeartbeat;
    private boolean open;
    private boolean lastRightPadActive;
    private int lastRightPadX;
    private int lastRightPadY;
    private float pointerDeltaX;
    private float pointerDeltaY;

    SteamController(SteamControllerDevice deviceInfo)
    {
        this.deviceInfo = deviceInfo;
        this.info = deviceInfo.toDeviceInfo();
    }

    @Override
    public boolean open()
    {
        if(this.open)
            return true;

        this.device = this.deviceInfo.hidDevice();
        if(!this.device.open())
        {
            Constants.LOG.warn("Unable to open Steam Controller HID path {}", this.deviceInfo.path());
            this.device = null;
            return false;
        }

        this.device.setNonBlocking(true);
        this.open = true;
        this.disableLizardMode();
        this.lastLizardHeartbeat = System.currentTimeMillis();
        return true;
    }

    @Override
    public void close()
    {
        if(this.device == null)
            return;

        try
        {
            this.restoreLizardMode();
        }
        catch(RuntimeException e)
        {
            Constants.LOG.debug("Unable to restore Steam Controller lizard mode", e);
        }

        this.device.close();
        this.device = null;
        this.open = false;
        this.snapshot = Snapshot.EMPTY;
        this.lastRightPadActive = false;
        this.pointerDeltaX = 0;
        this.pointerDeltaY = 0;
    }

    @Override
    public Number getJid()
    {
        return this.deviceInfo.jid();
    }

    @Override
    public boolean isOpen()
    {
        return this.open && this.device != null;
    }

    @Override
    public ButtonStates captureButtonStates()
    {
        this.pollReports();
        this.maintainLizardMode();

        Snapshot snapshot = this.snapshot;
        int raw = snapshot.buttons();
        ButtonStates states = new ButtonStates();
        states.setState(Buttons.A, has(raw, TRITON_A));
        states.setState(Buttons.B, has(raw, TRITON_B));
        states.setState(Buttons.X, has(raw, TRITON_X));
        states.setState(Buttons.Y, has(raw, TRITON_Y));
        states.setState(Buttons.SELECT, has(raw, TRITON_MENU));
        states.setState(Buttons.HOME, has(raw, TRITON_STEAM));
        states.setState(Buttons.START, has(raw, TRITON_VIEW));
        states.setState(Buttons.MISC, has(raw, TRITON_QAM));
        states.setState(Buttons.LEFT_THUMB_STICK, has(raw, TRITON_L3));
        states.setState(Buttons.RIGHT_THUMB_STICK, has(raw, TRITON_R3));
        states.setState(Buttons.LEFT_BUMPER, has(raw, TRITON_L));
        states.setState(Buttons.RIGHT_BUMPER, has(raw, TRITON_R));
        states.setState(Buttons.LEFT_TRIGGER, has(raw, TRITON_LEFT_TRIGGER_CLICK) || snapshot.leftTrigger() >= 0.5F);
        states.setState(Buttons.RIGHT_TRIGGER, has(raw, TRITON_RIGHT_TRIGGER_CLICK) || snapshot.rightTrigger() >= 0.5F);
        states.setState(Buttons.DPAD_UP, has(raw, TRITON_DPAD_UP));
        states.setState(Buttons.DPAD_DOWN, has(raw, TRITON_DPAD_DOWN));
        states.setState(Buttons.DPAD_LEFT, has(raw, TRITON_DPAD_LEFT));
        states.setState(Buttons.DPAD_RIGHT, has(raw, TRITON_DPAD_RIGHT));
        states.setState(Buttons.PADDLE_ONE, has(raw, TRITON_R4));
        states.setState(Buttons.PADDLE_TWO, has(raw, TRITON_L4));
        states.setState(Buttons.PADDLE_THREE, has(raw, TRITON_R5));
        states.setState(Buttons.PADDLE_FOUR, has(raw, TRITON_L5));
        states.setState(Buttons.TOUCHPAD, has(raw, TRITON_RIGHT_PAD_CLICK) || has(raw, TRITON_LEFT_PAD_CLICK));
        states.setState(Buttons.LEFT_THUMB_STICK_UP, snapshot.leftY() <= -0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_DOWN, snapshot.leftY() >= 0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_LEFT, snapshot.leftX() <= -0.5F);
        states.setState(Buttons.LEFT_THUMB_STICK_RIGHT, snapshot.leftX() >= 0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_UP, snapshot.rightY() <= -0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_DOWN, snapshot.rightY() >= 0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_LEFT, snapshot.rightX() <= -0.5F);
        states.setState(Buttons.RIGHT_THUMB_STICK_RIGHT, snapshot.rightX() >= 0.5F);

        // treat right pad click as primary select while a menu is open
        if(Minecraft.getInstance().screen != null && has(raw, TRITON_RIGHT_PAD_CLICK))
        {
            states.setState(Buttons.A, true);
        }
        return states;
    }

    @Override
    public String getName()
    {
        return this.deviceInfo.name();
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
        this.pollReports();
        return this.snapshot.leftTrigger();
    }

    @Override
    protected float internalGetRTriggerValue()
    {
        this.pollReports();
        return this.snapshot.rightTrigger();
    }

    @Override
    protected float internalGetLThumbStickXValue()
    {
        this.pollReports();
        return this.snapshot.leftX();
    }

    @Override
    protected float internalGetLThumbStickYValue()
    {
        this.pollReports();
        return this.snapshot.leftY();
    }

    @Override
    protected float internalGetRThumbStickXValue()
    {
        this.pollReports();
        return this.snapshot.rightX();
    }

    @Override
    protected float internalGetRThumbStickYValue()
    {
        this.pollReports();
        return this.snapshot.rightY();
    }

    @Override
    public DeviceInfo getInfo()
    {
        return this.info;
    }

    @Override
    public boolean supportsGyroscope()
    {
        return true;
    }

    @Override
    public Vector3f getGyroscope()
    {
        this.pollReports();
        Snapshot snapshot = this.snapshot;
        return new Vector3f(snapshot.gyroX(), snapshot.gyroY(), snapshot.gyroZ());
    }

    @Override
    public Vector2f consumeRelativePointerDelta()
    {
        this.pollReports();
        Vector2f delta = new Vector2f(this.pointerDeltaX, this.pointerDeltaY);
        this.pointerDeltaX = 0;
        this.pointerDeltaY = 0;
        return delta;
    }

    void tick()
    {
        this.pollReports();
        this.maintainLizardMode();
    }

    private void pollReports()
    {
        if(!this.isOpen())
            return;

        byte[] report = new byte[REPORT_SIZE];
        for(int i = 0; i < MAX_REPORTS_PER_POLL; i++)
        {
            Arrays.fill(report, (byte) 0);
            int read = this.device.read(report, 0);
            if(read == 0)
                return;

            if(read < 0)
            {
                this.open = false;
                return;
            }

            Snapshot next = this.parseReport(report, read);
            if(next != null)
            {
                this.applyRightPadPointer(next);
                this.snapshot = next;
            }
        }
    }

    private Snapshot parseReport(byte[] report, int length)
    {
        if(length <= 0)
            return null;

        int reportId = report[0] & 0xFF;
        if(reportId != ID_TRITON_CONTROLLER_STATE && reportId != ID_TRITON_CONTROLLER_STATE_BLE && reportId != ID_TRITON_CONTROLLER_STATE_TIMESTAMP)
            return null;

        if(length < 46)
            return null;

        int buttons = u32(report, 2);
        float leftTrigger = trigger(s16(report, 6));
        float rightTrigger = trigger(s16(report, 8));
        float leftX = axis(s16(report, 10));
        float leftY = axis(-s16(report, 12));
        float rightX = axis(s16(report, 14));
        float rightY = axis(-s16(report, 16));

        // report 0x47 shifts the pad fields by two bytes, but its shorter timestamp keeps gyro bytes aligned
        int rightPadOffset = reportId == ID_TRITON_CONTROLLER_STATE_TIMESTAMP ? 26 : 24;
        int imuOffset = reportId == ID_TRITON_CONTROLLER_STATE_TIMESTAMP ? 32 : 30;
        int gyroOffset = imuOffset + (reportId == ID_TRITON_CONTROLLER_STATE_TIMESTAMP ? 8 : 10);

        int rightPadX = s16(report, rightPadOffset);
        int rightPadY = s16(report, rightPadOffset + 2);
        int rightPadPressure = u16(report, rightPadOffset + 4);

        float gyroX = s16(report, gyroOffset) * GYRO_SCALE;
        float gyroY = s16(report, gyroOffset + 4) * GYRO_SCALE;
        float gyroZ = -s16(report, gyroOffset + 2) * GYRO_SCALE;

        return new Snapshot(buttons, leftTrigger, rightTrigger, leftX, leftY, rightX, rightY,
            rightPadX, rightPadY, rightPadPressure, gyroX, gyroY, gyroZ);
    }

    private void applyRightPadPointer(Snapshot next)
    {
        boolean active = has(next.buttons(), TRITON_RIGHT_PAD_TOUCH) || next.rightPadPressure() > 0;
        if(!active)
        {
            this.lastRightPadActive = false;
            return;
        }

        if(this.lastRightPadActive)
        {
            // pad reports absolute positions while mc needs motion deltas for cursor and camera control
            int dx = next.rightPadX() - this.lastRightPadX;
            int dy = next.rightPadY() - this.lastRightPadY;
            double magnitude = Math.sqrt(dx * (double) dx + dy * (double) dy);
            float acceleration = 1.0F + Math.min(2.0F, (float) magnitude / 5000.0F) * TRACKPAD_MOUSE_ACCELERATION;
            float scale = TRACKPAD_MOUSE_SCALE * acceleration;
            this.pointerDeltaX += dx * scale;
            this.pointerDeltaY += -dy * scale;
        }

        this.lastRightPadActive = true;
        this.lastRightPadX = next.rightPadX();
        this.lastRightPadY = next.rightPadY();
    }

    private void maintainLizardMode()
    {
        if(!this.isOpen())
            return;

        long now = System.currentTimeMillis();
        if(now - this.lastLizardHeartbeat >= LIZARD_HEARTBEAT_MS)
        {
            // keep lizard mode off while Controllable owns the reports
            this.sendFeatureCommand(ID_CLEAR_DIGITAL_MAPPINGS);
            this.sendLizardSetting(false);
            this.lastLizardHeartbeat = now;
        }
    }

    private void disableLizardMode()
    {
        this.sendFeatureCommand(ID_CLEAR_DIGITAL_MAPPINGS);
        this.sendFeatureCommand(ID_SET_SETTINGS_VALUES,
            15,
            SETTING_WIRELESS_PACKET_VERSION, 2, 0,
            SETTING_LEFT_TRACKPAD_MODE, TRACKPAD_NONE, 0,
            SETTING_RIGHT_TRACKPAD_MODE, TRACKPAD_NONE, 0,
            SETTING_SMOOTH_ABSOLUTE_MOUSE, 0, 0,
            SETTING_LIZARD_MODE, 0, 0);
        this.sendLizardSetting(false);
    }

    private void restoreLizardMode()
    {
        this.sendFeatureCommand(ID_SET_DEFAULT_DIGITAL_MAPPINGS);
        this.sendFeatureCommand(ID_LOAD_DEFAULT_SETTINGS, 0);
        this.sendLizardSetting(true);
    }

    private boolean sendLizardSetting(boolean enabled)
    {
        byte[] report = new byte[FEATURE_REPORT_SIZE];
        report[0] = (byte) ID_SET_SETTINGS_VALUES;
        report[1] = 3;
        report[2] = SETTING_LIZARD_MODE;
        report[3] = (byte) (enabled ? 1 : 0);
        return this.sendFeatureReport((byte) 1, report);
    }

    private boolean sendFeatureCommand(int command, int... payload)
    {
        byte[] report = new byte[FEATURE_REPORT_SIZE];
        report[0] = (byte) command;
        for(int i = 0; i < payload.length && i + 1 < report.length; i++)
        {
            report[i + 1] = (byte) payload[i];
        }
        return this.sendFeatureReport((byte) 0, report);
    }

    private boolean sendFeatureReport(byte reportId, byte[] report)
    {
        if(this.device == null)
            return false;

        // hid4java attaches HID report ID so report contains only the 64-byte Steam command payload
        int sent = this.device.sendFeatureReport(report, reportId);
        if(sent <= 0)
        {
            Constants.LOG.debug("Steam Controller feature report failed: command=0x{}", Integer.toHexString(report.length > 0 ? report[0] & 0xFF : 0));
            return false;
        }
        return true;
    }

    private static boolean has(int buttons, int mask)
    {
        return (buttons & mask) != 0;
    }

    private static float axis(int value)
    {
        return Mth.clamp(value / INT16_MAX, -1.0F, 1.0F);
    }

    private static float trigger(int value)
    {
        return Mth.clamp(value / INT16_MAX, 0.0F, 1.0F);
    }

    private static int u16(byte[] data, int offset)
    {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private static int s16(byte[] data, int offset)
    {
        int value = u16(data, offset);
        return value > 0x7FFF ? value - 0x10000 : value;
    }

    private static int u32(byte[] data, int offset)
    {
        return (data[offset] & 0xFF) |
            ((data[offset + 1] & 0xFF) << 8) |
            ((data[offset + 2] & 0xFF) << 16) |
            ((data[offset + 3] & 0xFF) << 24);
    }

    private record Snapshot(int buttons, float leftTrigger, float rightTrigger, float leftX, float leftY, float rightX, float rightY, int rightPadX, int rightPadY, int rightPadPressure, float gyroX, float gyroY, float gyroZ)
    {
        private static final Snapshot EMPTY = new Snapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
