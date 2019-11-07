package com.mrcrayfish.controllable.client.settings;

public enum EnumIcons {
    DualShock,
    xInput;

    // In-case the user edits the options manually, and enters an unknown value
    public static EnumIcons valueOfOrDefault(String value) {
        try {
            return valueOf(value);
        } catch (Exception e) {
            return DualShock;
        }
    }
}
