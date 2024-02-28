package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.binding.KeyUseOverride;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/**
 * Author: MrCrayfish
 */
@Mixin(Options.class)
public class OptionsMixin
{
    @Final
    @Mutable
    @Shadow
    public KeyMapping keyUse;

    @Final
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void controllableInit(Minecraft minecraft, File file, CallbackInfo ci)
    {
        // TODO: This causes an error because some casting appears to be broken in forge
        KeyMapping override = new KeyUseOverride(this.keyUse);
        for(int i = 0; i < this.keyMappings.length; i++)
        {
            if(this.keyMappings[i] == this.keyUse)
            {
                this.keyMappings[i] = override;
                this.keyUse = override;
                break;
            }
        }
    }
}
