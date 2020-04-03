package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public class Buttons
{
    public static final int LENGTH = 17;

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

    public static String buttonNameFromId(int id) {
        switch (id) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "X";
            case 3:
                return "Y";
            case 4:
                return "SELECT";
            case 5:
                return "HOME";
            case 6:
                return "START";
            case 7:
                return "LEFT_THUMB_STICK";
            case 8:
                return "RIGHT_THUMB_STICK";
            case 9:
                return "LEFT_BUMPER";
            case 10:
                return "RIGHT_BUMPER";
            case 11:
                return "LEFT_TRIGGER";
            case 12:
                return "RIGHT_TRIGGER";
            case 13:
                return "DPAD_UP";
            case 14:
                return "DPAD_DOWN";
            case 15:
                return "DPAD_LEFT";
            case 16:
                return "DPAD_RIGHT";
            default:
                throw new IndexOutOfBoundsException("The button ID" + id + " cannot be less than 0 or greater than " + (LENGTH - 1));
        }
    }
}
