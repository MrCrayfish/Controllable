package com.mrcrayfish.controllable.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.CursorType;
import com.mrcrayfish.controllable.client.ItemHeldBehaviour;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.controllable.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Author: MrCrayfish
 */
public class CursorOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        return Minecraft.getInstance().screen != null && Config.CLIENT.client.options.virtualCursor.get() && Controllable.getController() != null && Controllable.getInput().getLastUse() > 0 && !Controllable.getInput().isVirtualCursorHidden();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        CursorType type = Config.CLIENT.client.options.cursorType.get();
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || (mc.player.inventoryMenu.getCarried().isEmpty() || type.getBehaviour() == ItemHeldBehaviour.SHOW))
        {
            ControllerInput input = Controllable.getInput();
            double guiScale = mc.getWindow().getGuiScale();
            double virtualCursorX = input.getVirtualCursorX(mc.getFrameTime());
            double virtualCursorY = input.getVirtualCursorY(mc.getFrameTime());
            double zIndex = Services.PLATFORM.isForge() ? 300 : 3000; // Hack until I make Forge/Fabric calls the same
            pose.translate(virtualCursorX / guiScale, virtualCursorY / guiScale, zIndex);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean isHoveringSlot = input.getNearSlot() != null;
            if(isHoveringSlot)
            {
                pose.scale(1.33F, 1.33F, 1.33F);
            }
            graphics.blit(CursorType.TEXTURE, -8, -8, 16, 16, isHoveringSlot ? 32 : 0, type.ordinal() * 32, 32, 32, 64, CursorType.values().length * 32);
        }
        pose.popPose();
    }
}
