package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
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
    @Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void beforeUpdatePlayerLook(CallbackInfo ci)
    {
        if(Config.SERVER.restrictToController.get())
        {
            ci.cancel();
        }
    }
}
