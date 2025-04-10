package com.mrcrayfish.controllable.client.overlay;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.EventHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Author: MrCrayfish
 */
public class PaperDollPlayerOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        Controller controller = Controllable.getController();
        return !Minecraft.getInstance().options.hideGui && controller != null && (!Config.CLIENT.options.overlayTimeout.get() || controller.isBeingUsed());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, DeltaTracker tracker)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.screen == null && Config.CLIENT.options.paperDoll.get())
        {
            if(!EventHelper.postRenderMiniPlayer())
            {
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, 0, 0, 50, 70, 20, 0.0625F, 25, 35, mc.player);
            }
        }
    }
}
