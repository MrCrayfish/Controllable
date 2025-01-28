package com.mrcrayfish.controllable.client.overlay;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Author: MrCrayfish
 */
public interface IOverlay
{
    boolean isVisible();

    default void tick() {}

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
}
