package com.mrcrayfish.controllable.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.settings.CursorStyle;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.platform.Services;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Author: MrCrayfish
 */
public class VirtualCursorOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        Controller controller = Controllable.getController();
        return Minecraft.getInstance().getOverlay() == null && Minecraft.getInstance().screen != null && controller != null && controller.isBeingUsed() && Controllable.getCursor().isVisible();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, DeltaTracker tracker)
    {
        PoseStack pose = graphics.pose();
        pose.pushPose();

        Minecraft mc = Minecraft.getInstance();
        CursorStyle type = Config.CLIENT.options.cursorType.get();
        if(mc.player == null || (mc.player.inventoryMenu.getCarried().isEmpty() || type.getBehaviour() == CursorStyle.ItemHeldBehaviour.SHOW))
        {
            InputHandler input = Controllable.getInput();
            double guiScale = mc.getWindow().getGuiScale();
            double cursorX = Controllable.getCursor().getRenderX();
            double cursorY = Controllable.getCursor().getRenderY();
            double zIndex = Services.PLATFORM.isForge() ? 300 : 3000; // Hack until I make Forge/Fabric calls the same
            pose.translate(cursorX / guiScale, cursorY / guiScale, zIndex);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean isHoveringSlot = input.getNearSlot() != null;
            if(isHoveringSlot && type.isScaleHover())
            {
                pose.scale(1.33F, 1.33F, 1.33F);
            }
            graphics.blit(CursorStyle.TEXTURE, -8, -8, 16, 16, isHoveringSlot ? 32 : 0, type.ordinal() * 32, 32, 32, 64, CursorStyle.values().length * 32);
        }
        pose.popPose();
    }
}
