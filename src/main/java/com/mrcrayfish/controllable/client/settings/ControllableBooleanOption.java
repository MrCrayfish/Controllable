package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.IToolTip;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ControllableBooleanOption extends BooleanOption implements IToolTip
{
    private TranslationTextComponent toolTip;

    public ControllableBooleanOption(String title, Predicate<GameSettings> getter, BiConsumer<GameSettings, Boolean> setter)
    {
        super(title, getter, setter);
        this.toolTip = new TranslationTextComponent(title + ".desc");
    }

    @Override
    public void nextValue(GameSettings settings)
    {
        this.set(settings, String.valueOf(!this.get(settings)));
    }

    @Override
    public List<IReorderingProcessor> getToolTip()
    {
        return Minecraft.getInstance().fontRenderer.trimStringToWidth(this.toolTip, 200);
    }
}
