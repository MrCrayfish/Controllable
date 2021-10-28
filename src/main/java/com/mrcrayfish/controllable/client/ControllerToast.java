package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Author: MrCrayfish
 */
public class ControllerToast implements Toast
{
    private boolean connected;
    private Component controllerName;

    public ControllerToast(boolean connected, String controllerName)
    {
        this.connected = connected;
        this.controllerName = new TextComponent(controllerName);
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long delta)
    {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(poseStack, 0, 0, 0, 32, 160, 32);

        RenderSystem.setShaderTexture(0, ControllerLayoutScreen.TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(poseStack, 8, 8, 20, 43, 20, 16);

        String title = toastComponent.getMinecraft().font.plainSubstrByWidth(this.controllerName.getString(), 120);
        toastComponent.getMinecraft().font.draw(poseStack, title, 35, 7, 0);

        Component message = this.connected ?
                new TranslatableComponent("controllable.toast.connected").withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD) :
                new TranslatableComponent("controllable.toast.disconnected").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
        toastComponent.getMinecraft().font.draw(poseStack, message, 35, 18, 0);

        return delta >= 3000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
