package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ControllerManager;
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
    @Inject(method = "method_29338", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ResourceLoadStateTracker;finishReload()V", shift = At.Shift.AFTER))
    private void controllableOnFinishedLoading(CallbackInfo ci)
    {
        BindingRegistry.getInstance().load();
        ControllerManager.instance().onClientFinishedLoading();
    }
}
