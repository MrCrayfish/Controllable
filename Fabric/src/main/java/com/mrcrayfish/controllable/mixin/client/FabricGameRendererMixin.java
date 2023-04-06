package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class FabricGameRendererMixin
{
    /**
     * Fixes the x mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"), index = 1)
    private int controllableModifyMouseX(int mouseX)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            return (int) (input.getVirtualMouseX() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
        }
        return mouseX;
    }

    /**
     * Fixes the y mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"), index = 2)
    private int controllableModifyMouseY(int mouseY)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            return (int) (input.getVirtualMouseY() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
        }
        return mouseY;
    }
}
