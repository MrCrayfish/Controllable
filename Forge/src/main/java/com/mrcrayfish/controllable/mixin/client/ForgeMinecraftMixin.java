package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.framework.api.Environment;
import com.mrcrayfish.framework.api.util.EnvironmentHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO fabric version
/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class ForgeMinecraftMixin
{
    @Inject(method = "lambda$new$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ResourceLoadStateTracker;finishReload()V", shift = At.Shift.AFTER))
    private void controllableOnFinishedLoading(CallbackInfo ci)
    {
        if(EnvironmentHelper.getEnvironment() == Environment.CLIENT)
        {
            BindingRegistry.getInstance().load();
        }
    }
}
