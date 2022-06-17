package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHandler.class)
public class MouseHelperMixin
{
    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    private void beforeUpdateLook(long handle, double x, double y, CallbackInfo ci)
    {
        ControllerInput input = Controllable.getInput();
        if(input != null)
        {
            input.resetLastUse();
        }
    }

    @Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void beforeUpdatePlayerLook(CallbackInfo ci)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player != null && Config.SERVER.restrictToController.get())
        {
            ci.cancel();
        }
    }
}
