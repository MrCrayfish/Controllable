package com.mrcrayfish.controllable.client.overlay;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.framework.api.event.client.FrameworkClientTickEvents;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class OverlayRenderer
{
    private static final List<IOverlay> OVERLAYS = Util.make(() -> {
        ImmutableList.Builder<IOverlay> builder = new ImmutableList.Builder<>();
        builder.add(new TabNavigationOverlay());
        builder.add(new RecipeBookOverlay());
        builder.add(new PaperDollPlayerOverlay());
        builder.add(new RadialMenuOverlay());
        builder.add(new ActionHintOverlay());
        builder.add(new VirtualCursorOverlay());
        return builder.build();
    });

    public static void init()
    {
        FrameworkClientTickEvents.START_CLIENT.register(() -> OVERLAYS.forEach(IOverlay::tick));
    }

    public static void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, DeltaTracker tracker)
    {
        for(IOverlay overlay : OVERLAYS)
        {
            if(overlay.isVisible())
            {
                overlay.render(graphics, mouseX, mouseY, tracker);
            }
        }
    }
}
