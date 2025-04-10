package com.mrcrayfish.controllable.mixin.client.rei;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
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
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            double mouseX = Controllable.getCursor().getRenderScreenX();
            double mouseY = Controllable.getCursor().getRenderScreenY();
            cir.setReturnValue(new Point(mouseX, mouseY));
        }
    }

    @Inject(method = "ofFloatingMouse", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void controllableFloatMouse(CallbackInfoReturnable<FloatingPoint> cir)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isUsingVirtualCursor())
        {
            double mouseX = Controllable.getCursor().getRenderScreenX();
            double mouseY = Controllable.getCursor().getRenderScreenY();
            cir.setReturnValue(new FloatingPoint(mouseX, mouseY));
        }
    }
}