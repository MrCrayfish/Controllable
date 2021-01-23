package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHelper.class)
public class MouseHelperMixin
{
    @Inject(method = "cursorPosCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHelper;updatePlayerLook()V"))
    private void beforeUpdateLook(long handle, double x, double y, CallbackInfo ci)
    {
        Controllable.getInput().resetLastUse();
    }
}
