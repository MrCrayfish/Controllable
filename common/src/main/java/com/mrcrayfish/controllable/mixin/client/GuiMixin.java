package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(Gui.class)
public class GuiMixin
{
    @ModifyExpressionValue(method = "renderTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean controllableRenderPlayerList(boolean original)
    {
        return original || Controllable.getController() != null && ButtonBindings.PLAYER_LIST.isButtonDown();
    }
}
