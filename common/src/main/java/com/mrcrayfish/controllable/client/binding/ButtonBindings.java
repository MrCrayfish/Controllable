package com.mrcrayfish.controllable.client.binding;

import com.mrcrayfish.controllable.client.input.Buttons;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump", "key.categories.movement", BindingContext.IN_GAME);
    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.sneak", "key.categories.movement", BindingContext.IN_GAME);
    public static final ButtonBinding SPRINT = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sprint", "key.categories.movement", BindingContext.IN_GAME);
    public static final ButtonBinding OPEN_INVENTORY = new ButtonBinding(Buttons.Y, "controllable.key.open_inventory", "key.categories.inventory", BindingContext.IN_GAME);
    public static final ButtonBinding CLOSE_INVENTORY = new ButtonBinding(Buttons.Y, "controllable.key.close_inventory", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapOffhand", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.DPAD_LEFT, "key.pickItem", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist", "key.categories.multiplayer", BindingContext.IN_GAME);
    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot", "key.categories.misc", BindingContext.GLOBAL);
    public static final ButtonBinding SCROLL_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previous_hotbar_item", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding SCROLL_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.next_hotbar_item", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding PAUSE_GAME = new ButtonBinding(Buttons.START, "controllable.key.pause_game", "key.categories.misc", BindingContext.GLOBAL);
    public static final ButtonBinding NEXT_CREATIVE_TAB = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previous_creative_tab", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding PREVIOUS_CREATIVE_TAB = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.next_creative_tab", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding NEXT_RECIPE_TAB = new ButtonBinding(Buttons.LEFT_TRIGGER, "controllable.key.previous_recipe_tab", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding PREVIOUS_RECIPE_TAB = new ButtonBinding(Buttons.RIGHT_TRIGGER, "controllable.key.next_recipe_tab", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding NAVIGATE_UP = new ButtonBinding(Buttons.DPAD_UP, "controllable.key.move_up", "key.categories.ui", BindingContext.IN_SCREEN);
    public static final ButtonBinding NAVIGATE_DOWN = new ButtonBinding(Buttons.DPAD_DOWN, "controllable.key.move_down", "key.categories.ui", BindingContext.IN_SCREEN);
    public static final ButtonBinding NAVIGATE_LEFT = new ButtonBinding(Buttons.DPAD_LEFT, "controllable.key.move_left", "key.categories.ui", BindingContext.IN_SCREEN);
    public static final ButtonBinding NAVIGATE_RIGHT = new ButtonBinding(Buttons.DPAD_RIGHT, "controllable.key.move_right", "key.categories.ui", BindingContext.IN_SCREEN);
    public static final ButtonBinding PICKUP_ITEM = new ButtonBinding(Buttons.A, "controllable.key.pickup_item", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding QUICK_MOVE = new ButtonBinding(Buttons.B, "controllable.key.quick_move", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding SPLIT_STACK = new ButtonBinding(Buttons.X, "controllable.key.split_stack", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding SOCIAL_INTERACTIONS = new ButtonBinding(-1, "key.socialInteractions", "key.categories.multiplayer", BindingContext.IN_GAME);
    public static final ButtonBinding ADVANCEMENTS = new ButtonBinding(-1, "key.advancements", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HIGHLIGHT_PLAYERS = new ButtonBinding(-1, "key.spectatorOutlines", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding CINEMATIC_CAMERA = new ButtonBinding(-1, "key.smoothCamera", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding FULLSCREEN = new ButtonBinding(-1, "key.fullscreen", "key.categories.misc", BindingContext.GLOBAL);
    public static final ButtonBinding DEBUG_INFO = new ButtonBinding(-1, "controllable.key.debug_info", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding RADIAL_MENU = new ButtonBinding(Buttons.DPAD_RIGHT, "controllable.key.radial_menu", "key.categories.gameplay", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_1 = new ButtonBinding(-1, "controllable.key.hotbar_slot_1", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_2 = new ButtonBinding(-1, "controllable.key.hotbar_slot_2", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_3 = new ButtonBinding(-1, "controllable.key.hotbar_slot_3", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_4 = new ButtonBinding(-1, "controllable.key.hotbar_slot_4", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_5 = new ButtonBinding(-1, "controllable.key.hotbar_slot_5", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_6 = new ButtonBinding(-1, "controllable.key.hotbar_slot_6", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_7 = new ButtonBinding(-1, "controllable.key.hotbar_slot_7", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_8 = new ButtonBinding(-1, "controllable.key.hotbar_slot_8", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding HOTBAR_SLOT_9 = new ButtonBinding(-1, "controllable.key.hotbar_slot_9", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding[] HOTBAR_SLOTS = {HOTBAR_SLOT_1, HOTBAR_SLOT_2, HOTBAR_SLOT_3, HOTBAR_SLOT_4, HOTBAR_SLOT_5, HOTBAR_SLOT_6, HOTBAR_SLOT_7, HOTBAR_SLOT_8, HOTBAR_SLOT_9};
    public static final ButtonBinding TOGGLE_CRAFT_BOOK = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "controllable.key.toggle_craft_book", "key.categories.inventory", BindingContext.IN_SCREEN);
    public static final ButtonBinding OPEN_CONTROLLABLE_SETTINGS = new ButtonBinding(-1, "controllable.key.open_controllable_settings", "key.categories.misc", BindingContext.IN_GAME);
    public static final ButtonBinding OPEN_CHAT = new ButtonBinding(-1, "key.chat", "key.categories.multiplayer", BindingContext.IN_GAME);
}
