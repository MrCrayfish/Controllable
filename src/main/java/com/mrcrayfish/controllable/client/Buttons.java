package com.mrcrayfish.controllable.client;

import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class Buttons
{
    public static final int A = 0;
    public static final int B = 1;
    public static final int X = 2;
    public static final int Y = 3;
    public static final int SELECT = 4;
    public static final int HOME = 5;
    public static final int START = 6;
    public static final int LEFT_THUMB_STICK = 7;
    public static final int RIGHT_THUMB_STICK = 8;
    public static final int LEFT_BUMPER = 9;
    public static final int RIGHT_BUMPER = 10;
    public static final int LEFT_TRIGGER = 11;
    public static final int RIGHT_TRIGGER = 12;
    public static final int DPAD_UP = 13;
    public static final int DPAD_DOWN = 14;
    public static final int DPAD_LEFT = 15;
    public static final int DPAD_RIGHT = 16;
    public static final int LENGTH = 17;
    public static final int[] BUTTONS = {A, B, X, Y, SELECT, HOME, START, LEFT_THUMB_STICK, RIGHT_THUMB_STICK, LEFT_BUMPER, RIGHT_BUMPER, LEFT_TRIGGER, RIGHT_TRIGGER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT};
    public static final String[] NAMES = {
        "controllable.button.a",
        "controllable.button.b",
        "controllable.button.x",
        "controllable.button.y",
        "controllable.button.select",
        "controllable.button.home",
        "controllable.button.start",
        "controllable.button.left_thumb_stick",
        "controllable.button.right_thumb_stick",
        "controllable.button.left_bumper",
        "controllable.button.right_bumper",
        "controllable.button.left_trigger",
        "controllable.button.right_trigger",
        "controllable.button.dpad_up",
        "controllable.button.dpad_down",
        "controllable.button.dpad_left",
        "controllable.button.dpad_right"
    };

    @Nullable
    public static String getNameForButton(int button)
    {
        if(button < 0 || button >= LENGTH)
            return null;
        return NAMES[button];
    }

    public static int getButtonFromName(String name)
    {
        return ArrayUtils.indexOf(NAMES, name);
    }
}
