package com.mrcrayfish.controllable.registry;


import net.minecraft.client.settings.KeyBinding;


public class ActionDataDescription
{

    private String actionKey;
    private String categoryKey;

    public ActionDataDescription(KeyBinding keyBinding) {
        this(keyBinding.getKeyDescription(), keyBinding.getKeyCategory());
    }

    public ActionDataDescription(String actionKey, KeyBinding keyBinding) {
        this(actionKey, keyBinding.getKeyCategory());
    }

    public ActionDataDescription(String actionKey, String categoryKey)
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
