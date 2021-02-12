package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

/**
 * Author: MrCrayfish
 */
public class ControllerToast implements IToast
{
    private boolean connected;
    private String controllerName;

    public ControllerToast(boolean connected, String controllerName)
    {
        this.connected = connected;
        this.controllerName = controllerName;
    }

    @Override
    public Visibility draw(ToastGui toastGui, long delta)
    {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        toastGui.blit(0, 0, 0, 32, 160, 32);

        toastGui.getMinecraft().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
        toastGui.blit(8, 8, 20, 43, 20, 16);

        String title = toastGui.getMinecraft().fontRenderer.trimStringToWidth(this.controllerName, 120); //TODO test
        toastGui.getMinecraft().fontRenderer.drawString(TextFormatting.DARK_GRAY + title, 35, 7, 0);

        String message = this.connected ?
                TextFormatting.DARK_GREEN.toString() + TextFormatting.BOLD.toString() + I18n.format("controllable.toast.connected") :
                TextFormatting.RED.toString() + TextFormatting.BOLD.toString() + I18n.format("controllable.toast.disconnected");
        toastGui.getMinecraft().fontRenderer.drawString(TextFormatting.BOLD + message, 35, 18, 0);

        return delta >= 3000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
