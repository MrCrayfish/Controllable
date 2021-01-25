package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump", "key.categories.movement");
    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.sneak", "key.categories.movement");
    public static final ButtonBinding SPRINT = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sprint", "key.categories.movement");
    public static final ButtonBinding INVENTORY = new ButtonBinding(Buttons.Y, "key.inventory", "key.categories.inventory");
    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapOffhand", "key.categories.inventory");
    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop", "key.categories.inventory");
    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use", "key.categories.gameplay");
    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack", "key.categories.gameplay");
    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.DPAD_LEFT, "key.pickItem", "key.categories.gameplay");
    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist", "key.categories.multiplayer");
    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective", "key.categories.misc");
    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot", "key.categories.misc");
    public static final ButtonBinding SCROLL_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "key.previousHotbarItem", "key.categories.inventory");
    public static final ButtonBinding SCROLL_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "key.nextHotbarItem", "key.categories.inventory");

    // TODO prevent start button from being remapped
    public static final ButtonBinding PAUSE_GAME = new ButtonBinding(Buttons.START, "key.pauseGame", "key.categories.gameplay", true);

    public static List<ButtonBinding> getBindings()
    {
        return ImmutableList.copyOf(ButtonBinding.BINDINGS);
    }
}
