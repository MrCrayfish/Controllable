package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Author: MrCrayfish
 */
@Mixin(EntityRenderer.class)
public class GameRendererMixin
{
    /**
     * Fixes the mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArgs(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/GuiScreen;IIF)V", remap = false))
    private void drawScreen(Args args)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(minecraft);
            int mouseX = (int) (input.getVirtualMouseX() * (double) resolution.getScaledWidth() / (double) minecraft.displayWidth);
            int mouseY = (int) (input.getVirtualMouseY() * (double) resolution.getScaledHeight() / (double) minecraft.displayHeight);
            args.set(1, mouseX);
            args.set(2, mouseY);
        }
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayInGameMenu()V"))
    private void onPause(Minecraft minecraft)
    {
        if(Controllable.getController() == null || !Controllable.getOptions().isVirtualMouse())
        {
            minecraft.displayInGameMenu();
        }
    }
}
