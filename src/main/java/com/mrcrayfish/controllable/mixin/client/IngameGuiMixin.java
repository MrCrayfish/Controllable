package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import net.minecraft.client.gui.IngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(IngameGui.class)
public class IngameGuiMixin
{
    /**
     * Fixes selected item name rendering not being offset by console hotbar
     */
    @Inject(method = "renderHotbar", at = @At(value = "HEAD"))
    private void beforeRenderHotbarCall(float partialTicks, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            RenderSystem.translated(0, -20, 0);
        }
    }

    @Inject(method = "renderHotbar", at = @At(value = "TAIL"))
    private void afterRenderHotbarCall(float partialTicks, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            RenderSystem.translated(0, 20, 0);
        }
    }
}
