package com.mrcrayfish.controllable.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.CursorStyle;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Author: MrCrayfish
 */
public class VirtualCursorOverlay implements IOverlay
{
    @Override
    public boolean isVisible()
    {
        Controller controller = Controllable.getController();
        return Minecraft.getInstance().getOverlay() == null && Minecraft.getInstance().screen != null && controller != null && controller.isUsingVirtualCursor() && Controllable.getCursor().isVisible();
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
            double guiScale = mc.getWindow().getGuiScale();
            double cursorX = Controllable.getCursor().getRenderX();
            double cursorY = Controllable.getCursor().getRenderY();
            double zIndex = 3000;
            pose.translate(cursorX / guiScale, cursorY / guiScale, zIndex);
            boolean isHoveringSlot = this.isHoveringFilledContainerSlot();
            if(isHoveringSlot && type.isScaleHover())
            {
                pose.scale(1.33F, 1.33F, 1.33F);
            }
            graphics.blit(CursorStyle.TEXTURE, -8, -8, 16, 16, isHoveringSlot ? 32 : 0, type.ordinal() * 32, 32, 32, 64, CursorStyle.values().length * 32);
        }
        pose.popPose();
    }

    /**
     * @return True if the cursor is hovering a container slot
     */
    private boolean isHoveringFilledContainerSlot()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen instanceof AbstractContainerScreen<?> screen)
        {
            Slot slot = ClientServices.CLIENT.getSlotUnderMouse(screen);
            return slot != null && slot.hasItem();
        }
        return false;
    }
}
