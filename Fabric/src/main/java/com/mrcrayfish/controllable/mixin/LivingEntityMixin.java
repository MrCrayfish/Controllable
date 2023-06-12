package com.mrcrayfish.controllable.mixin;

import com.mrcrayfish.controllable.client.RumbleHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "updateUsingItem", at = @At(value = "HEAD"))
    private void controllableOnUsingItem(ItemStack stack, CallbackInfo ci)
    {
        if(!stack.isEmpty())
        {
            LivingEntity entity = (LivingEntity) (Object) this;
            RumbleHandler.onPlayerUsingItem(entity, stack, entity.getUseItemRemainingTicks());
        }
    }
}
