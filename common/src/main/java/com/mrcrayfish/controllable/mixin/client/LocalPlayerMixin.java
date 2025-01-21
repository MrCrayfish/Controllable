package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin
{
    @Unique
    private float controllable$lastHealth;

    @Inject(method = "hurtTo", at = @At(value = "HEAD"))
    private void controllableOnPlayerDamage(float newHealth, CallbackInfo ci)
    {
        if(newHealth < this.controllable$lastHealth)
        {
            float damage = Math.max(0, this.controllable$lastHealth - newHealth);
            LocalPlayer player = (LocalPlayer) (Object) this;
            Controllable.getRumbleHandler().onDamage(player, damage);
        }
        this.controllable$lastHealth = newHealth;
    }
}
