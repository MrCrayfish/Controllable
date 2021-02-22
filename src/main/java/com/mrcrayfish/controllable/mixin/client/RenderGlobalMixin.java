package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.ButtonBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(RenderGlobal.class)
public class RenderGlobalMixin
{
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "isOutlineActive", at = @At(value = "HEAD"), cancellable = true)
    private void isOutlineActive(Entity entityIn, Entity viewer, ICamera camera, CallbackInfoReturnable<Boolean> cir)
    {
        if(this.mc.player != null && this.mc.player.isSpectator() && ButtonBindings.HIGHLIGHT_PLAYERS.isButtonDown() && entityIn instanceof EntityPlayer)
        {
            if(entityIn.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entityIn.getEntityBoundingBox()) || entityIn.isRidingOrBeingRiddenBy(this.mc.player))
            {
                cir.setReturnValue(true);
            }
        }
    }
}
