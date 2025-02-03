package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Author: MrCrayfish
 */
@Mixin(ExtendedGui.class)
public class NeoForgeGuiMixin
{
    /**
     * Fixes record name rendering not being offset by console hotbar
     */
    @ModifyExpressionValue(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean controllableRenderPlayerList(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && ButtonBindings.PLAYER_LIST.isButtonDown())
        {
            return true;
        }
        return original;
    }
}
