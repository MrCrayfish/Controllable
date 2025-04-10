package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"))
    private void controllableLastRender(DeltaTracker tracker, boolean running, CallbackInfo ci, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY, @Local GuiGraphics graphics)
    {
        OverlayRenderer.draw(graphics, mouseX, mouseY, tracker);
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false, ordinal = 0), index = 4, ordinal = 0, require = 1)
    private int controllableModifyMouseX(int original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            return (int) Controllable.getCursor().getRenderScreenX();
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false, ordinal = 0), index = 5, ordinal = 1, require = 1)
    private int controllableModifyMouseY(int original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            return (int) Controllable.getCursor().getRenderScreenY();
        }
        return original;
    }
}
