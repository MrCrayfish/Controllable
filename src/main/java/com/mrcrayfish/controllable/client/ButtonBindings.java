package com.mrcrayfish.controllable.client;

import net.minecraftforge.client.settings.KeyConflictContext;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.sneak", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SPRINT = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sprint", "key.categories.movement", KeyConflictContext.IN_GAME);
    public static final ButtonBinding INVENTORY = new ButtonBinding(Buttons.Y, "key.inventory", "key.categories.inventory", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapOffhand", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.DPAD_LEFT, "key.pickItem", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist", "key.categories.multiplayer", KeyConflictContext.IN_GAME);
    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot", "key.categories.misc", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding SCROLL_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previousHotbarItem", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding SCROLL_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.nextHotbarItem", "key.categories.gameplay", KeyConflictContext.IN_GAME);
    public static final ButtonBinding PAUSE_GAME = new ButtonBinding(Buttons.START, "controllable.key.pauseGame", "key.categories.misc", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding NEXT_CREATIVE_TAB = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previousCreativeTab", "key.categories.inventory", KeyConflictContext.GUI);
    public static final ButtonBinding PREVIOUS_CREATIVE_TAB = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.nextCreativeTab", "key.categories.inventory", KeyConflictContext.GUI);
    public static final ButtonBinding NEXT_RECIPE_TAB = new ButtonBinding(Buttons.LEFT_TRIGGER, "controllable.key.previousRecipeTab", "key.categories.inventory", KeyConflictContext.GUI);
    public static final ButtonBinding PREVIOUS_RECIPE_TAB = new ButtonBinding(Buttons.RIGHT_TRIGGER, "controllable.key.nextRecipeTab", "key.categories.inventory", KeyConflictContext.GUI);
    public static final ButtonBinding NAVIGATE_UP = new ButtonBinding(Buttons.DPAD_UP, "controllable.key.moveUp", "key.categories.ui", KeyConflictContext.GUI);
    public static final ButtonBinding NAVIGATE_DOWN = new ButtonBinding(Buttons.DPAD_DOWN, "controllable.key.moveDown", "key.categories.ui", KeyConflictContext.GUI);
    public static final ButtonBinding NAVIGATE_LEFT = new ButtonBinding(Buttons.DPAD_LEFT, "controllable.key.moveLeft", "key.categories.ui", KeyConflictContext.GUI);
    public static final ButtonBinding NAVIGATE_RIGHT = new ButtonBinding(Buttons.DPAD_RIGHT, "controllable.key.moveRight", "key.categories.ui", KeyConflictContext.GUI);
    public static final ButtonBinding PICKUP_ITEM = new ButtonBinding(Buttons.A, "controllable.key.pickupItem", "key.categories.ui", KeyConflictContext.GUI, true);
    public static final ButtonBinding QUICK_MOVE = new ButtonBinding(Buttons.B, "controllable.key.quickMove", "key.categories.ui", KeyConflictContext.GUI, true);
    public static final ButtonBinding SPLIT_STACK = new ButtonBinding(Buttons.X, "controllable.key.splitStack", "key.categories.ui", KeyConflictContext.GUI, true);
    public static final ButtonBinding ADVANCEMENTS = new ButtonBinding(-1, "key.advancements", "key.categories.misc", KeyConflictContext.IN_GAME);
    public static final ButtonBinding HIGHLIGHT_PLAYERS = new ButtonBinding(-1, "key.spectatorOutlines", "key.categories.misc", KeyConflictContext.IN_GAME);
    public static final ButtonBinding CINEMATIC_CAMERA = new ButtonBinding(-1, "key.smoothCamera", "key.categories.misc", KeyConflictContext.IN_GAME);
    public static final ButtonBinding FULLSCREEN = new ButtonBinding(-1, "key.fullscreen", "key.categories.misc", KeyConflictContext.UNIVERSAL);
    public static final ButtonBinding DEBUG_INFO = new ButtonBinding(-1, "controllable.key.debugInfo", "key.categories.misc", KeyConflictContext.IN_GAME);
}
