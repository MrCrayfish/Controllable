package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.overlay.ActionHintOverlay;
import com.mrcrayfish.controllable.client.overlay.IOverlay;
import com.mrcrayfish.controllable.client.overlay.PlayerOverlay;
import com.mrcrayfish.controllable.client.overlay.RecipeBookOverlay;
import com.mrcrayfish.controllable.client.overlay.TabNavigationOverlay;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class OverlayHandler
{
    private static final List<IOverlay> OVERLAYS = Util.make(() -> {
        ImmutableList.Builder<IOverlay> builder = new ImmutableList.Builder<>();
        builder.add(new TabNavigationOverlay());
        builder.add(new RecipeBookOverlay());
        builder.add(new ActionHintOverlay());
        builder.add(new PlayerOverlay());
        return builder.build();
    });

    public static void init()
    {
        TickEvents.START_CLIENT.register(() -> OVERLAYS.forEach(IOverlay::tick));
    }

    public static void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        for(IOverlay overlay : OVERLAYS)
        {
            if(overlay.isVisible())
            {
                overlay.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }
}
