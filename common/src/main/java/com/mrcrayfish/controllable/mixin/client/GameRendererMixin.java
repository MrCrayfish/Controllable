package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.overlay.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    @Shadow
    @Final
    private Minecraft minecraft;

    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void controllableLastRender(DeltaTracker tracker, boolean running, CallbackInfo ci, @Local GuiGraphics graphics)
    {
        int mouseX = (int) this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
        int mouseY = (int) this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
        OverlayRenderer.draw(graphics, mouseX, mouseY, tracker);
    }*/

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false, ordinal = 0), index = 4, ordinal = 0, require = 1)
    private int controllableModifyMouseX(int original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            return (int) Controllable.getCursor().getRenderScreenX();
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false, ordinal = 0), index = 5, ordinal = 1, require = 1)
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
