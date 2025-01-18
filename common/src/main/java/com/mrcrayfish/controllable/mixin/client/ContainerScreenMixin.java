package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Author: MrCrayfish
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin
{
    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(JI)Z", ordinal = 0))
    private boolean isQuickMovePressedOnClick(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && ButtonBindings.QUICK_MOVE.isButtonDown())
        {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(JI)Z", ordinal = 0))
    private boolean isQuickMovePressedOnReleased(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && ButtonBindings.QUICK_MOVE.isButtonDown())
        {
            return true;
        }
        return original;
    }
}
