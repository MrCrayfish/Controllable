package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.OverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class OptifineGameRendererMixin
{
    /**
     * Fixes the mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/optifine/reflect/Reflector;callVoid(Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)V", remap = false), index = 1)
    @SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature", "UnresolvedMixinReference"})
    private Object[] controllableDrawScreen(Object[] args)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            int mouseX = (int) (input.getVirtualCursorX() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getWidth());
            int mouseY = (int) (input.getVirtualCursorY() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getHeight());
            args[2] = mouseX;
            args[3] = mouseY;
        }
        return args;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void controllableLastRender(float partialTick, long p_109095_, boolean running, CallbackInfo ci, int mouseX, int mouseY, Window window, float guiFarPlane, Matrix4f matrix4f, PoseStack posestack, float guiOffsetZ, GuiGraphics graphics)
    {
        OverlayHandler.draw(graphics, mouseX, mouseY, partialTick);
    }
}
