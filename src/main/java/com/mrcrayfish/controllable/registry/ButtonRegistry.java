package com.mrcrayfish.controllable.registry;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ButtonRegistry {

    private Map<String, ButtonBinding> buttonBindings = new HashMap<>();



    public ButtonBinding getButton(String action) {
        if (!buttonBindings.containsKey(action)) throw new ButtonNotFoundException(action, "The action " + action + " does not have a button set to it in the registry.");

        return buttonBindings.get(action);
    }

    public ButtonBinding registerButton(String action, ButtonBinding buttonBinding) {
        if (buttonBindings.containsKey(action)) {
            ButtonBinding oldButton = getButton(action);
            throw new ButtonExistsException(action, oldButton.getButton(), "Action " + action + " already taken by button " + oldButton.getButton());
        }

        return buttonBindings.get(action);
    }

    public ButtonBinding swapAction(String action, ButtonBinding buttonBinding) {
        if (!buttonBindings.containsKey(action)) throw new ButtonNotFoundException(action, "The action " + action + " does not have a button set to it in the registry. Swapping requires buttons to be registered");

    }

    public void registerDefaults() {
        for (ButtonActions action : ButtonActions.values()) {
            registerButton(action.action, new ButtonBinding(action.button, action.keyDescription));
        }
    }




    
    @AllArgsConstructor
    public enum ButtonActions {
        JUMP("JUMP", Buttons.A, "key.jump"),
        SNEAK("SNEAK", Buttons.LEFT_THUMB_STICK, "key.sneak"),
        SPRINT("SPRINT", -1, "key.sprint"),
        INVENTORY("INVENTORY", Buttons.Y, "key.inventory"),
        SWAP_HANDS("SWAP_HANDS", Buttons.X, "key.swapHands"),
        DROP_ITEM("DROP_ITEM", Buttons.DPAD_DOWN, "key.drop"),
        USE_ITEM("USE_ITEM", Buttons.LEFT_TRIGGER, "key.use"),
        ATTACK("ATTACK", Buttons.RIGHT_TRIGGER, "key.attack"),
        PICK_BLOCK("PICK_BLOCK", Buttons.RIGHT_THUMB_STICK, "key.pickItem"),
        PLAYER_LIST("PLAYER_LIST", Buttons.SELECT, "key.playerlist"),
        TOGGLE_PERSPECTIVE("TOGGLE_PERSPECTIVE", Buttons.DPAD_UP, "key.togglePerspective"),
        SCREENSHOT("SCREENSHOT", -1, "key.screenshot"),
        SCROLL_LEFT("SCROLL_LEFT", Buttons.LEFT_BUMPER, "key.scrollLeft"),
        SCROLL_RIGHT("SCROLL_RIGHT", Buttons.RIGHT_BUMPER, "key.scrollRight"),
        QUICK_MOVE("QUICK_MOVE", Buttons.B, "key.quickMove"),
        PAUSE_GAME("PAUSE_GAME", Buttons.START, "key.pauseGame");

        private String action;

        @Getter
        private int button;

        private String keyDescription;

        public ButtonBinding getButton() {
            return Controllable.getButtonRegistry().getButton(action);
        }
    }

}
