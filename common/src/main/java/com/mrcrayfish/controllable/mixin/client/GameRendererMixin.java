package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique
    private int controllable$captureMouseX;

    @Unique
    private int controllable$captureMouseY;

    @Unique
    private GuiGraphicsExtractor controllable$extractor;

    @Inject(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    private void controllable$captureLocals(DeltaTracker tracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY, @Local(ordinal = 0) GuiGraphicsExtractor extractor)
    {
        this.controllable$captureMouseX = mouseX;
        this.controllable$captureMouseY = mouseY;
        this.controllable$extractor = extractor;
    }

    @Inject(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractDeferredSubtitles()V"))
    private void controllable$renderOverlay(DeltaTracker tracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci)
    {
        if(this.controllable$extractor != null)
        {
            OverlayRenderer.draw(this.controllable$extractor, this.controllable$captureMouseX, this.controllable$captureMouseY, tracker);
            this.controllable$extractor = null;
        }
    }

    @ModifyExpressionValue(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;getScaledXPos(Lcom/mojang/blaze3d/platform/Window;)D", ordinal = 0))
    private double controllable$ModifyMouseX(double original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            return (int) Controllable.getCursor().getRenderScreenX();
        }
        return original;
    }

    @ModifyExpressionValue(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;getScaledYPos(Lcom/mojang/blaze3d/platform/Window;)D", ordinal = 0))
    private double controllable$ModifyMouseY(double original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            return (int) Controllable.getCursor().getRenderScreenY();
        }
        return original;
    }
}
