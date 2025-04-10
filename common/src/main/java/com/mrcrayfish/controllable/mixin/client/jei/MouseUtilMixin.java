package com.mrcrayfish.controllable.mixin.client.jei;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import mezz.jei.gui.input.MouseUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JEI has a utility class to get the X and Y position of the mouse when it's not possible to
 * retrieve them. This causes issues when virtual mouse is turned on (which is most of the time).
 * This fixes compatibility with the mod.
 *
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(MouseUtil.class)
public class MouseUtilMixin
{
    @Inject(method = "getX", at = @At(value = "TAIL"), remap = false, cancellable = true)
    private static void controllableGetX(CallbackInfoReturnable<Double> cir)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            cir.setReturnValue(Controllable.getCursor().getRenderScreenX());
        }
    }

    @Inject(method = "getY", at = @At(value = "TAIL"), remap = false, cancellable = true)
    private static void controllableGetY(CallbackInfoReturnable<Double> cir)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            cir.setReturnValue(Controllable.getCursor().getRenderScreenY());
        }
    }
}
