package com.mrcrayfish.controllable.client.overlay;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Author: MrCrayfish
 */
public class RadialMenuOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, DeltaTracker tracker)
    {
        Controllable.getRadialMenu().onRenderEnd(graphics, tracker);
    }
}
