package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerToggleSetting extends ControllerSetting<Boolean>
{
    public ControllerToggleSetting(String key, ForgeConfigSpec.ConfigValue<Boolean> configValue)
    {
        super(key, configValue);
    }

    @Override
    public Supplier<AbstractWidget> createWidget(int x, int y, int width, int height)
    {
        return () -> CycleButton.onOffBuilder(this.configValue.get()).withTooltip(value -> this.tooltip).create(x, y, width, height, this.label, (button, value) -> {
            this.configValue.set(value);
            Config.save();
        });
    }
}
