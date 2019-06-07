package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump");
    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sneak");
    public static final ButtonBinding SPRINT = new ButtonBinding(-1, "key.sprint");
    public static final ButtonBinding INVENTORY = new ButtonBinding(Buttons.Y, "key.inventory");
    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapHands");
    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop");
    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use");
    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack");
    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.pickItem");
    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist");
    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective");
    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot");
    public static final ButtonBinding SCROLL_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "key.scrollLeft");
    public static final ButtonBinding SCROLL_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "key.scrollRight");
}
