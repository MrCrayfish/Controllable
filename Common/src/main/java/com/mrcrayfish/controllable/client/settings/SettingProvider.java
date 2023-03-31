package com.mrcrayfish.controllable.client.settings;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public interface SettingProvider
{
    Supplier<AbstractWidget> createWidget(int x, int y, int width, int height);
}
