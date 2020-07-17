package com.mrcrayfish.controllable.registry;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;

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
    private Map<String, ActionDataDescription> actionTranslateMap = new HashMap<>();

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

    /**
     * Registers the button with it's action and button binding instance.
     *
     *
     *
     * @param action The name of the action. This is basically the key that will be used for retrieving and storing in the registry
     * @param translateKey The description of the action. It states in what category and what the action does.
     * @param buttonBinding The button binding instance. This will handle the state management of the button such as pressed or button id.
     * @return The button binding instance is returned back
     */
    public ButtonBinding registerButton(String action, ActionDataDescription translateKey, ButtonBinding buttonBinding) {
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

        buttonBindings.put(action, buttonBinding);

        return buttonBinding;
    }

    public ActionDataDescription getAction(String action) {
        if (!actionTranslateMap.containsKey(action)) {
            // Theoretically, this should never happen unless an oversight in
            // the api allows it
            throw new IllegalArgumentException("The action " + action + " is not registered. Register by registering a button");
        }
        return actionTranslateMap.get(action);
    }

    /**
     * Returns a copy of the button bindings
     * @return a copy
     */
    public Map<String, ButtonBinding> getButtonBindings() {
        return new HashMap<>(buttonBindings);
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

}
