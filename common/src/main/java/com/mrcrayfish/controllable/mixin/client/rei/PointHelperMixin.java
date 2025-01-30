package com.mrcrayfish.controllable.mixin.client.rei;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(PointHelper.class)
public class PointHelperMixin
{
    @Inject(method = "ofMouse", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void controllableMouse(CallbackInfoReturnable<Point> cir)
    {
        ControllerInput input = Controllable.getInput();
        if(input.isVirtualCursorActive())
        {
            double mouseX = input.getScaledCursorX();
            double mouseY = input.getScaledCursorY();
            cir.setReturnValue(new Point(mouseX, mouseY));
        }
    }

    @Inject(method = "ofFloatingMouse", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void controllableFloatMouse(CallbackInfoReturnable<FloatingPoint> cir)
    {
        ControllerInput input = Controllable.getInput();
        if(input.isVirtualCursorActive())
        {
            double mouseX = input.getScaledCursorX();
            double mouseY = input.getScaledCursorY();
            cir.setReturnValue(new FloatingPoint(mouseX, mouseY));
        }
    }
}
