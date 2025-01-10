package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class FabricGameRendererMixin
{
    /**
     * Fixes the x mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), index = 1)
    private int controllableModifyMouseX(int mouseX)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            return (int) Controllable.getCursor().getRenderScreenX();
        }
        return mouseX;
    }

    /**
     * Fixes the y mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), index = 2)
    private int controllableModifyMouseY(int mouseY)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            return (int) Controllable.getCursor().getRenderScreenY();
        }
        return mouseY;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"))
    private void controllableLastRender(DeltaTracker tracker, boolean bl, CallbackInfo ci, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY, @Local GuiGraphics graphics)
    {
        OverlayRenderer.draw(graphics, mouseX, mouseY, tracker);
    }
}
