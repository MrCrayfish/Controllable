package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
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
    public Visibility draw(GuiToast toastGui, long delta)
    {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 32, 160, 32);

        toastGui.getMinecraft().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
        toastGui.drawTexturedModalRect(8, 8, 20, 43, 20, 16);

        String title = toastGui.getMinecraft().fontRenderer.trimStringToWidth(this.controllerName, 120);
        toastGui.getMinecraft().fontRenderer.drawString(TextFormatting.DARK_GRAY + title, 35, 7, 0);

        String message = this.connected ?
                TextFormatting.DARK_GREEN.toString() + TextFormatting.BOLD.toString() + I18n.format("controllable.toast.connected") :
                TextFormatting.RED.toString() + TextFormatting.BOLD.toString() + I18n.format("controllable.toast.disconnected");
        toastGui.getMinecraft().fontRenderer.drawString(TextFormatting.BOLD + message, 35, 18, 0);

        return delta >= 3000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
