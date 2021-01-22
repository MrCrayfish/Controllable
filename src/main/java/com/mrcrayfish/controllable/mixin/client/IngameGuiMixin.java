package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
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
    @Inject(method = "func_238453_b_", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V", shift = At.Shift.AFTER))
    private void afterPushMatrix(MatrixStack matrixStack, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            RenderSystem.translated(0, -20, 0);
        }
    }
}
