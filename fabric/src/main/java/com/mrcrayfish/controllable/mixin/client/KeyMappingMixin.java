package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin
{
    @Shadow
    private InputConstants.Key key;

    @Shadow
    private boolean isDown;

    @Shadow
    public abstract String getName();

    @Inject(method = "isDown", at = @At(value = "HEAD"), cancellable = true)
    private void controllableIsDown(CallbackInfoReturnable<Boolean> cir)
    {
        if(this.isDown && this.controllableIsActiveAndMatches(this.key))
        {
            cir.setReturnValue(true);
        }
    }

    // TODO needs testing
    private boolean controllableIsActiveAndMatches(InputConstants.Key keyCode)
    {
        String customKey = this.getName() + ".custom";
        KeyAdapterBinding adapter = BindingRegistry.getInstance().getKeyAdapters().get(customKey);
        if(adapter != null && adapter.isButtonDown())
        {
            return true;
        }
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(this.key);
    }
}
