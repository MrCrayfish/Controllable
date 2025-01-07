package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
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
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            return (int) (input.getVirtualCursorX() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
        }
        return mouseX;
    }

    @ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreenInternal(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V", remap = false), index = 3, remap = false)
    private static int controllableModifyMouseY(int mouseY)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            return (int) (input.getVirtualCursorY() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
        }
        return mouseY;
    }
}
