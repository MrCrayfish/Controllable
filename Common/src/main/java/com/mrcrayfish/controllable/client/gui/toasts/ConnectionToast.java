package com.mrcrayfish.controllable.client.gui.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class ConnectionToast implements Toast
{
    private final boolean connected;
    private final Component controllerName;

    public ConnectionToast(boolean connected, String controllerName)
    {
        this.connected = connected;
        this.controllerName = Component.literal(controllerName);
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long delta)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, 0, 0, 0, 32, 160, 32);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(ControllerLayoutScreen.TEXTURE, 8, 8, 20, 43, 20, 16);

        String title = toastComponent.getMinecraft().font.plainSubstrByWidth(this.controllerName.getString(), 120);
        graphics.drawString(toastComponent.getMinecraft().font, title, 35, 7, 0);

        Component message = this.connected ?
                Component.translatable("controllable.toast.connected").withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD) :
                Component.translatable("controllable.toast.disconnected").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
        graphics.drawString(toastComponent.getMinecraft().font, message, 35, 18, 0);

        return delta >= 3000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
