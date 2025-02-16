package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.client.ClientEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* DO NOT DO THIS. THIS IS LAST RESORT SINCE PRE EVENT CAN BE CANCELED */
@Mixin(GuiLayerManager.class)
public class GuiLayerManagerMixin
{
    @Inject(method = "renderInner", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDraw$Layer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void controllableBeforeRenderLayer(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo info, @Local(index = 4, ordinal = 0) GuiLayerManager.NamedLayer layer)
    {
        ClientEvents.beforeRenderLayer(graphics, layer);
    }
}
