package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.ButtonBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin
{
    @Inject(method = "func_225510_bt_", at = @At(value = "HEAD"), cancellable = true)
    private void isEntityGlowing(CallbackInfoReturnable<Boolean> cir)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.player.isSpectator() && ButtonBindings.HIGHLIGHT_PLAYERS.isButtonDown())
        {
            cir.setReturnValue(true);
        }
    }
}
