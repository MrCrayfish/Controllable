package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class ForgeMinecraftMixin
{
    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void controllableOnFinishedLoading(CallbackInfo ci)
    {
        BindingRegistry.getInstance().load();
        Controllable.getManager().onClientFinishedLoading();
    }
}
