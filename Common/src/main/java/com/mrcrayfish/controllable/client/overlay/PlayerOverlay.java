package com.mrcrayfish.controllable.client.overlay;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.util.EventHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Author: MrCrayfish
 */
public class PlayerOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        return !Minecraft.getInstance().options.hideGui && Controllable.getController() != null && Controllable.getInput().getLastUse() > 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.screen == null && Config.CLIENT.client.options.renderMiniPlayer.get())
        {
            if(!EventHelper.postRenderMiniPlayer())
            {
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, 20, 45, 20, 0, 0, mc.player);
            }
        }
    }
}
