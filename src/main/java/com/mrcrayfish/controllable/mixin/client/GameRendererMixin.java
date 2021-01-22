package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    /**
     * Fixes the mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArgs(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screen/Screen;Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V", remap = false))
    private void drawScreen(Args args)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            int mouseX = (int) (input.getVirtualMouseX() * (double) minecraft.getMainWindow().getScaledWidth() / (double) minecraft.getMainWindow().getWidth());
            int mouseY = (int) (input.getVirtualMouseY() * (double) minecraft.getMainWindow().getScaledHeight() / (double) minecraft.getMainWindow().getHeight());
            args.set(2, mouseX);
            args.set(3, mouseY);
        }
    }
}
