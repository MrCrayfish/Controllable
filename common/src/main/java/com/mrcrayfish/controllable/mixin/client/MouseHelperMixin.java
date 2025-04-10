package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHandler.class)
public abstract class MouseHelperMixin
{
    @Unique
    private boolean controllable$releaseBypass;

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Shadow
    public abstract void releaseMouse();

    /*
     * In Controllable, the mouse remains grabbed (aka hidden) when in screens like the inventory.
     * However, this allows the player to turn the camera while in a screen, something that isn't
     * normally possible. To fix that, as soon as we detect movement from the mouse, we make it
     * appear again.
     */
    @Inject(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), cancellable = true)
    private void controllableBeforeUpdateLook(CallbackInfo ci)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.screen != null)
        {
            if(Math.abs(this.accumulatedDX) > 0 || Math.abs(this.accumulatedDY) > 0)
            {
                this.controllable$releaseBypass = true;
                this.releaseMouse(); // Release mouse since it may be grabbed
                this.controllable$releaseBypass = false;
                this.accumulatedDX = 0;
                this.accumulatedDY = 0;
                ci.cancel();
            }
        }
    }

    /*
     * Prevents the cursor from being released when opening screens when using a controller. Since
     * Controllable uses a virtual cursor, it doesn't make sense to have the system cursor appear
     * when opening a screen (like the inventory).
     */
    @Inject(method = "releaseMouse", at = @At(value = "HEAD"), cancellable = true)
    private void controllableGrabCursor(CallbackInfo ci)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor() && !this.controllable$releaseBypass)
        {
            ci.cancel();
        }
    }
}
