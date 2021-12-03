package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.BindingRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NEVER DO THIS! I REALLY DIDN'T WANT TO DO THIS BUT JEI (Just Enough Items) LOADS IT'S KEY
 * BINDINGS DURING FMLLoadCompleteEvent EVENT WHICH HAPPENS AFTER I TRY TO LOAD BUTTON BINDINGS
 * FROM FILE. Feel free to PR
 *
 * Author: MrCrayfish
 */
@Mixin(ClientModLoader.class)
public class ClientModLoaderMixin
{
    @Inject(method = "completeModLoading", at = @At(value = "HEAD"), remap = false)
    private static void completeModLoadingHead(CallbackInfoReturnable<Boolean> cir)
    {
        if(FMLLoader.getDist() == Dist.CLIENT)
        {
            BindingRegistry.getInstance().load();
        }
    }
}
