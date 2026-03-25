package com.mrcrayfish.controllable.client.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Author: MrCrayfish
 */
public interface IOverlay
{
    boolean isVisible();

    default void tick() {}

    void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, DeltaTracker tracker);
}
