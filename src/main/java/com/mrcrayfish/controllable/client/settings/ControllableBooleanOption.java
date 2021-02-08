package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.BooleanOption;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ControllableBooleanOption extends BooleanOption
{
    public ControllableBooleanOption(String title, Predicate<GameSettings> getter, BiConsumer<GameSettings, Boolean> setter)
    {
        super(title, getter, setter);
    }

    @Override
    public void nextValue(GameSettings settings)
    {
        this.set(settings, String.valueOf(!this.get(settings)));
    }
}
