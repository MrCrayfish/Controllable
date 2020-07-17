package com.mrcrayfish.controllable.registry;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;

public class ControllableButtons
{
    /**
     * Used to define a constant of slots in inventory.
     * If Minecraft ever changes this number, this constant should be modified
     */
    private static final int INVENTORY_HOTBAR_SLOT_NUMBERS = 9;
    private static final String TRANSLATE_SLOT_KEY = "key.hotbar.";
    private static ButtonBinding[] slotButtonBindings;

    /**
     * Registers the default buttons of the mod
     */
    public static void registerDefaults()
    {
        for(ControllableButtons.ButtonActions action : ControllableButtons.ButtonActions.values())
        {
            Controllable.getButtonRegistry().registerButton(action.action, action.actionDataDescription, new ButtonBinding(action.getButtonId()));
        }

        slotButtonBindings = new ButtonBinding[INVENTORY_HOTBAR_SLOT_NUMBERS];
        for(int i = 0; i < INVENTORY_HOTBAR_SLOT_NUMBERS; i++)
        {

            slotButtonBindings[i] = Controllable.getButtonRegistry().registerButton("hotbar." + i, new ActionDataDescription(TRANSLATE_SLOT_KEY + (i + 1), Minecraft.getInstance().gameSettings.keyBindsHotbar[0]), new ButtonBinding(-1));
        }
    }

    public enum ButtonActions {
        JUMP("JUMP", Buttons.A, new ActionDataDescription(getGameSettings().keyBindJump)),
        SNEAK("SNEAK", Buttons.LEFT_THUMB_STICK, new ActionDataDescription(getGameSettings().keyBindSneak)),
        SPRINT("SPRINT", -1, new ActionDataDescription(getGameSettings().keyBindSprint)),
        INVENTORY("INVENTORY", Buttons.Y, new ActionDataDescription(getGameSettings().keyBindInventory)),
        SWAP_HANDS("SWAP_HANDS", Buttons.X, new ActionDataDescription(getGameSettings().keyBindSwapHands)),
        DROP_ITEM("DROP_ITEM", Buttons.DPAD_DOWN, new ActionDataDescription(getGameSettings().keyBindDrop)),
        USE_ITEM("USE_ITEM", Buttons.LEFT_TRIGGER, new ActionDataDescription(getGameSettings().keyBindUseItem)),
        ATTACK("ATTACK", Buttons.RIGHT_TRIGGER, new ActionDataDescription(getGameSettings().keyBindAttack)),
        PICK_BLOCK("PICK_BLOCK", Buttons.RIGHT_THUMB_STICK, new ActionDataDescription(getGameSettings().keyBindPickBlock)),
        PLAYER_LIST("PLAYER_LIST", Buttons.SELECT, new ActionDataDescription(getGameSettings().keyBindPlayerList)),
        TOGGLE_PERSPECTIVE("TOGGLE_PERSPECTIVE", Buttons.DPAD_UP, new ActionDataDescription(getGameSettings().keyBindTogglePerspective)),
        SCREENSHOT("SCREENSHOT", -1, new ActionDataDescription(getGameSettings().keyBindScreenshot)),
        SCROLL_LEFT("SCROLL_LEFT", Buttons.LEFT_BUMPER, new ActionDataDescription("controllable.action.scroll_left", "key.categories.gameplay")),
        SCROLL_RIGHT("SCROLL_RIGHT", Buttons.RIGHT_BUMPER, new ActionDataDescription("controllable.action.scroll_right", "key.categories.gameplay")),
        QUICK_MOVE("QUICK_MOVE", Buttons.B, new ActionDataDescription("controllable.action.quick_move", "key.categories.gameplay")),
        PAUSE_GAME("PAUSE_GAME", Buttons.START, new ActionDataDescription("key.keyboard.pause", "key.categories.gameplay")),
        OPEN_CHAT("OPEN_CHAT", -1, new ActionDataDescription(getGameSettings().keyBindChat)),
        OPEN_COMMAND_CHAT("OPEN_COMMAND_CHAT", -1, new ActionDataDescription(getGameSettings().keyBindCommand)),
        SMOOTH_CAMERA_TOGGLE("SMOOTH_CAMERA_TOGGLE", -1, new ActionDataDescription(getGameSettings().keyBindSmoothCamera)),

        ;


        private String action;

        private int buttonId;

        private ActionDataDescription actionDataDescription;

        private static GameSettings getGameSettings() {
            return Minecraft.getInstance().gameSettings;
        }

        public ButtonBinding getButton() {
            return Controllable.getButtonRegistry().getButton(action);
        }

        ButtonActions(String action, int buttonId, ActionDataDescription actionDataDescription)
        {
            this.action = action;
            this.buttonId = buttonId;
            this.actionDataDescription = actionDataDescription;
        }

        public int getButtonId()
        {
            return buttonId;
        }
    }

    /**
     *
     * @return copy
     */
    public static ButtonBinding[] getSlotButtonBindings()
    {
        return slotButtonBindings.clone();
    }
}
