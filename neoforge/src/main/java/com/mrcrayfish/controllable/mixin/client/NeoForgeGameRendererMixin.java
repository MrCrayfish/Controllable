package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NeoForgeGameRendererMixin
{
    @Unique
    private int controllable$captureMouseX;

    @Unique
    private int controllable$captureMouseY;

    @Unique
    private GuiGraphics controllable$graphics;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    private void captureLocals(DeltaTracker tracker, boolean p_109096_, CallbackInfo ci, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY, @Local(ordinal = 0) GuiGraphics graphics)
    {
        this.controllable$captureMouseX = mouseX;
        this.controllable$captureMouseY = mouseY;
        this.controllable$graphics = graphics;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void renderOverlay(DeltaTracker tracker, boolean p_109096_, CallbackInfo ci)
    {
        if(this.controllable$graphics != null)
        {
            OverlayRenderer.draw(this.controllable$graphics, this.controllable$captureMouseX, this.controllable$captureMouseY, tracker);
            this.controllable$graphics = null;
        }
    }
}
