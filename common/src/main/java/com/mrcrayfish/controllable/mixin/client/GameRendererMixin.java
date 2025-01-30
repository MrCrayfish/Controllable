package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Author: MrCrayfish
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"))
    private void controllableOnPause(Minecraft mc, boolean pauseOnly)
    {
        if(Controllable.getController() == null || !Config.CLIENT.client.options.virtualCursor.get())
        {
            mc.pauseGame(false);
        }
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false), index = 5, ordinal = 0, require = 1)
    private int controllableModifyMouseX(int original)
    {
        ControllerInput input = Controllable.getInput();
        if(input.isVirtualCursorActive())
        {
            return (int) input.getScaledCursorX();
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false), index = 6, ordinal = 1, require = 1)
    private int controllableModifyMouseY(int original)
    {
        ControllerInput input = Controllable.getInput();
        if(input.isVirtualCursorActive())
        {
            return (int) input.getScaledCursorY();
        }
        return original;
    }
}
