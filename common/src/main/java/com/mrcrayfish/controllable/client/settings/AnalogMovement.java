package com.mrcrayfish.controllable.client.settings;

/**
 * Author: MrCrayfish
 */
public enum AnalogMovement implements SettingEnum
{
    DISABLED("controllable.analog_movement.disabled"),
    LOCAL_ONLY("controllable.analog_movement.local_only"),
    ALWAYS("controllable.analog_movement.always");

    private final String key;

    AnalogMovement(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }
}
