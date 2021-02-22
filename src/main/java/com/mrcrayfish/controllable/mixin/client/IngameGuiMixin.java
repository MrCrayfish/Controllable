package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(GuiIngame.class)
public class IngameGuiMixin
{
    /**
     * Fixes selected item name rendering not being offset by console hotbar
     */
    @Inject(method = "renderSelectedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void afterPushMatrix(ScaledResolution scaledRes, CallbackInfo ci)
    {
        if(Controllable.getOptions().useConsoleHotbar())
        {
            GlStateManager.translate(0, -20, 0);
        }
    }
}
