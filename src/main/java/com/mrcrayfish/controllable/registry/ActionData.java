package com.mrcrayfish.controllable.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;

@Data
@Getter
@AllArgsConstructor
public class ActionData {

    private String actionKey;
    private String categoryKey;

    public ActionData(KeyBinding keyBinding) {
        this(keyBinding.getKeyDescription(), keyBinding.getKeyCategory());
    }

    public ActionData(String actionKey, KeyBinding keyBinding) {
        this(keyBinding.getKeyDescription(), keyBinding.getKeyCategory());
    }

}
