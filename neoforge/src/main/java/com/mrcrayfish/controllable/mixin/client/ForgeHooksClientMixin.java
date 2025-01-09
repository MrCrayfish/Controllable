package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientHooks.class)
public class ForgeHooksClientMixin
{
    @ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreenInternal(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V", remap = false), index = 2, remap = false)
    private static int controllableModifyMouseX(int mouseX)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            Minecraft mc = Minecraft.getInstance();
            double cursorX = Controllable.getCursor().getRenderX();
            return (int) (cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth());
        }
        return mouseX;
    }

    @ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreenInternal(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V", remap = false), index = 3, remap = false)
    private static int controllableModifyMouseY(int mouseY)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed())
        {
            Minecraft mc = Minecraft.getInstance();
            double cursorY = Controllable.getCursor().getRenderY();
            return (int) (cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight());
        }
        return mouseY;
    }
}
