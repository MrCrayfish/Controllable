package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrcrayfish.controllable.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Author: MrCrayfish
 */
@Mixin(Hud.class)
public class FabricHudMixin
{
    @WrapMethod(method = "extractHotbarAndDecorations")
    private void consoleHotbarOffsetHead(GuiGraphicsExtractor extractor, DeltaTracker tracker, Operation<Void> original)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            Matrix3x2fStack stack = extractor.pose();
            stack.pushMatrix();
            stack.translate(0, -25);
            original.call(extractor, tracker);
            stack.popMatrix();
        }
        else
        {
            original.call(extractor, tracker);
        }
    }
}
