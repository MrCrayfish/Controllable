package com.mrcrayfish.controllable.registry;


import net.minecraft.client.settings.KeyBinding;


public class ActionData {

    private String actionKey;
    private String categoryKey;

    public ActionData(KeyBinding keyBinding) {
        this(keyBinding.getKeyDescription(), keyBinding.getKeyCategory());
    }

    public ActionData(String actionKey, KeyBinding keyBinding) {
        this(actionKey, keyBinding.getKeyCategory());
    }

    public ActionData(String actionKey, String categoryKey)
    {
        this.actionKey = actionKey;
        this.categoryKey = categoryKey;
    }

    public String getActionTranslateKey()
    {
        return actionKey;
    }

    public String getCategoryTranslateKey()
    {
        return categoryKey;
    }
}
