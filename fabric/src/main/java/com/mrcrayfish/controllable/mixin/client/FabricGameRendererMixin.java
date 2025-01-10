package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
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
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), index = 1)
    private int controllableModifyMouseX(int mouseX)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            return (int) Controllable.getCursor().getRenderScreenX();
        }
        return mouseX;
    }

    /**
     * Fixes the y mouse position when virtual mouse is turned on for controllers.
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), index = 2)
    private int controllableModifyMouseY(int mouseY)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            return (int) Controllable.getCursor().getRenderScreenY();
        }
        return mouseY;
    }
}
