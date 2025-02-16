package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
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
public class FabricGuiMixin
{
    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "HEAD"))
    private void consoleHotbarOffsetHead(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            graphics.pose().pushPose();
            graphics.pose().translate(0, -25, 0);
        }
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "TAIL"))
    private void consoleHotbarOffsetTail(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            graphics.pose().popPose();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At(value = "HEAD"))
    private void consoleExpLevelHead(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            graphics.pose().pushPose();
            graphics.pose().translate(0, -25, 0);
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At(value = "TAIL"))
    private void consoleExpLevelTail(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            graphics.pose().popPose();
        }
    }
}
