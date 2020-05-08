package com.mrcrayfish.controllable.registry;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Fernthedev
 * {@link "https://github.com/Fernthedev"}
 */
public class ButtonRegistry {

    // ACTION_NAME:BUTTON_BINDING
    private Map<String, ButtonBinding> buttonBindings = new HashMap<>();

    /**
     * The map where actions get translated to Minecraft translation keys
     * ACTION_NAME:DATA
     */
    private Map<String, ActionData> actionTranslateMap = new HashMap<>();

    /**
     * Load the button mappings from the config file
     * @param buttonRegistryConfig
     */
    public void loadFromConfig(@Nonnull Config<ButtonConfigData> buttonRegistryConfig) throws ConfigLoadException {

        ButtonConfigData configData = buttonRegistryConfig.syncLoad();

        configData.buttonMap.forEach((action, buttonId) -> {
            if (buttonBindings.containsKey(action)) {
                Controllable.LOGGER.debug("Setting {} to {} before was {}", action, buttonId, buttonBindings.get(action).getButtonId());
                buttonBindings.get(action).setButton(buttonId);
            }
        });

    }

    /**
     *
     * @param function The function will return a config instance of any choice and will be given the {@link ButtonConfigData} instance to instantiate it
     * @param override If true, the mapping will override with the loaded button mapping
     *
     * @return The config instance for reusing
     * @throws ConfigLoadException Thrown when the config is malformed.
     */
    public Config<ButtonConfigData> saveMappings(Function<ButtonConfigData, Config<ButtonConfigData>> function, boolean override) throws ConfigLoadException {
        @Nonnull Map<String, Integer> saveConfigMap = new HashMap<>();

        buttonBindings.forEach((action, buttonBinding) -> saveConfigMap.put(action, buttonBinding.getButtonId()));

        ButtonConfigData buttonConfigData = new ButtonConfigData(saveConfigMap);

        Config<ButtonConfigData> config = function.apply(buttonConfigData); // Instantiate config instance with the default value as buttonConfigData

        @Nonnull Map<String, Integer> configButtonMap = config.syncLoad().buttonMap; // Loaded from config

        if (!override) {
            Controllable.LOGGER.warn("Saving without overriding using current settings");

            configButtonMap.forEach(saveConfigMap::put);
        }

        buttonConfigData = new ButtonConfigData(saveConfigMap);
        config.setConfigData(buttonConfigData);

        Controllable.LOGGER.debug("Saving {}", buttonConfigData);

        config.syncSave();

        loadFromConfig(config);

        return config;
    }

    /**
     * Gets the button associated with the action
     * @param action
     * @return the button
     */
    public ButtonBinding getButton(String action) {
        if (!buttonBindings.containsKey(action)) throw new ButtonNotFoundException(action, "The action " + action + " does not have a button set to it in the registry.");

        return buttonBindings.get(action);
    }

    public ButtonBinding registerButton(String action, ActionData translateKey, ButtonBinding buttonBinding) {
        if (buttonBindings.containsKey(action)) {
            ButtonBinding oldButton = getButton(action);
            throw new ButtonExistsException(action, oldButton.getButtonId(), "Action " + action + " already taken by button " + oldButton.getButtonId());
        }

        if (actionTranslateMap.containsKey(action)) {
            // Theoretically, this should never happen unless an oversight in
            // the api allows it
            throw new IllegalStateException("The action " + action + " is already registered which should not be the case. Bug in Controllable or mod?");
        }

        actionTranslateMap.put(action, translateKey);

        return buttonBindings.put(action, buttonBinding);
    }

    public ActionData getAction(String action) {
        if (!actionTranslateMap.containsKey(action)) {
            // Theoretically, this should never happen unless an oversight in
            // the api allows it
            throw new IllegalArgumentException("The action " + action + " is not registered. Register by registering a button");
        }
        return actionTranslateMap.get(action);
    }

    /**
     * Returns a copy of the button bindings
     * @return
     */
    public Map<String, ButtonBinding> getButtonBindings() {
        return new HashMap<>(buttonBindings);
    }

    public void registerDefaults() {
        for (ButtonActions action : ButtonActions.values()) {
            registerButton(action.action, action.actionData, new ButtonBinding(action.getButtonId(), action.getButtonId()));
        }
    }


    public static class ButtonConfigData {
        // Action:ButtonID
        private @Nonnull Map<String, Integer> buttonMap;

        public ButtonConfigData(@Nonnull Map<String, Integer> buttonMap)
        {
            this.buttonMap = buttonMap;
        }

        @Nonnull
        public Map<String, Integer> getButtonMap()
        {
            return buttonMap;
        }
    }

    

    public enum ButtonActions {
        JUMP("JUMP", Buttons.A, new ActionData(getGameSettings().keyBindJump)),
        SNEAK("SNEAK", Buttons.LEFT_THUMB_STICK, new ActionData(getGameSettings().keyBindSneak)),
        SPRINT("SPRINT", -1, new ActionData(getGameSettings().keyBindSprint)),
        INVENTORY("INVENTORY", Buttons.Y, new ActionData(getGameSettings().keyBindInventory)),
        SWAP_HANDS("SWAP_HANDS", Buttons.X, new ActionData(getGameSettings().keyBindSwapHands)),
        DROP_ITEM("DROP_ITEM", Buttons.DPAD_DOWN, new ActionData(getGameSettings().keyBindDrop)),
        USE_ITEM("USE_ITEM", Buttons.LEFT_TRIGGER, new ActionData(getGameSettings().keyBindUseItem)),
        ATTACK("ATTACK", Buttons.RIGHT_TRIGGER, new ActionData(getGameSettings().keyBindAttack)),
        PICK_BLOCK("PICK_BLOCK", Buttons.RIGHT_THUMB_STICK, new ActionData(getGameSettings().keyBindPickBlock)),
        PLAYER_LIST("PLAYER_LIST", Buttons.SELECT, new ActionData(getGameSettings().keyBindPlayerList)),
        TOGGLE_PERSPECTIVE("TOGGLE_PERSPECTIVE", Buttons.DPAD_UP, new ActionData(getGameSettings().keyBindTogglePerspective)),
        SCREENSHOT("SCREENSHOT", -1, new ActionData(getGameSettings().keyBindScreenshot)),
        SCROLL_LEFT("SCROLL_LEFT", Buttons.LEFT_BUMPER, new ActionData("controllable.action.scroll_left", "key.categories.gameplay")),
        SCROLL_RIGHT("SCROLL_RIGHT", Buttons.RIGHT_BUMPER, new ActionData("controllable.action.scroll_right", "key.categories.gameplay")),
        QUICK_MOVE("QUICK_MOVE", Buttons.B, new ActionData("controllable.action.quick_move", "key.categories.gameplay")),
        PAUSE_GAME("PAUSE_GAME", Buttons.START, new ActionData("key.keyboard.pause", "key.categories.gameplay")),
        OPEN_CHAT("OPEN_CHAT", -1, new ActionData(getGameSettings().keyBindChat)),
        OPEN_COMMAND_CHAT("OPEN_COMMAND_CHAT", -1, new ActionData(getGameSettings().keyBindCommand)),
        SMOOTH_CAMERA_TOGGLE("SMOOTH_CAMERA_TOGGLE", -1, new ActionData(getGameSettings().keyBindSmoothCamera)),

        ;


        private String action;

        private int buttonId;

        private ActionData actionData;

        private static GameSettings getGameSettings() {
            return Minecraft.getInstance().gameSettings;
        }

        public ButtonBinding getButton() {
            return Controllable.getButtonRegistry().getButton(action);
        }

        ButtonActions(String action, int buttonId, ActionData actionData)
        {
            this.action = action;
            this.buttonId = buttonId;
            this.actionData = actionData;
        }

        public int getButtonId()
        {
            return buttonId;
        }
    }

}
