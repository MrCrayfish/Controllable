package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class FabricMinecraftMixin
{
    @Inject(method = "onGameLoadFinished", at = @At(value = "HEAD"))
    private void controllableGameLoaded(CallbackInfo ci)
    {
        Controllable.getBindingRegistry().completeSetup();
        Controllable.getControllerManager().completeSetup();
        Controllable.getCursor().resetToCenter();
    }
}
