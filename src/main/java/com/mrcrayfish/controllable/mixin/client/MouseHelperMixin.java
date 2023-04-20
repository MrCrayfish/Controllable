package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHandler.class)
public abstract class MouseHelperMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void releaseMouse();

    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    private void controllableBeforeUpdateLook(long handle, double x, double y, CallbackInfo ci)
    {
        Minecraft minecraft = Minecraft.getInstance();
        ControllerInput input = Controllable.getInput();
        if(input != null && !input.isMovingCursor() && minecraft.screen != null)
        {
            input.resetLastUse();
            this.releaseMouse(); // Release mouse since it may be grabbed
        }
    }

    /* Prevents the cursor from being released when opening screens when using a controller */
    @Inject(method = "releaseMouse", at = @At(value = "HEAD"), cancellable = true)
    private void controllableGrabCursor(CallbackInfo ci)
    {
        ControllerInput input = Controllable.getInput();
        if(input.isControllerInUse())
        {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), cancellable = true)
    private void controllableTurn(CallbackInfo ci)
    {
        if(this.minecraft.player == null)
        {
            ci.cancel();
        }
    }
}
