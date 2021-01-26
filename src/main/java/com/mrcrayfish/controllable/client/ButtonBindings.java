package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.sneak", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SPRINT = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sprint", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding INVENTORY = new ButtonBinding(Buttons.Y, "key.inventory", "key.categories.inventory", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapOffhand", "key.categories.inventory", KeyConflictContext.IN_GAME);
    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop", "key.categories.inventory", KeyConflictContext.IN_GAME);
    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.DPAD_LEFT, "key.pickItem", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist", "key.categories.multiplayer", KeyConflictContext.IN_GAME);
    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective", "key.categories.misc", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot", "key.categories.misc", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding SCROLL_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previousHotbarItem", "key.categories.inventory", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SCROLL_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.nextHotbarItem", "key.categories.inventory", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PAUSE_GAME = new ButtonBinding(Buttons.START, "controllable.key.pauseGame", "key.categories.misc", KeyConflictContext.UNIVERSAL);

    static
    {
        ButtonBinding.resetBindingHash();
    }

    public static List<ButtonBinding> getBindings()
    {
        return ImmutableList.copyOf(ButtonBinding.BINDINGS);
    }
}
