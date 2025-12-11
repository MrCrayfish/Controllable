package com.mrcrayfish.controllable.client.gui.toasts;

import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Author: MrCrayfish
 */
public class ConnectionToast implements Toast
{
    private static final Identifier BACKGROUND_SPRITE = Utils.resource("toast/connection");
    private static final Component LABEL_CONNECTED = Component.translatable("controllable.toast.connected").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD);
    private static final Component LABEL_DISCONNECTED = Component.translatable("controllable.toast.disconnected").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);

    private final boolean connected;
    private final Component controllerName;
    private Visibility visibility = Visibility.HIDE;

    public ConnectionToast(boolean connected, String controllerName)
    {
        this.connected = connected;
        this.controllerName = Component.literal(controllerName);
    }

    @Override
    public Visibility getWantedVisibility()
    {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long delta)
    {
        long displayTime = (long) (3000L * manager.getNotificationDisplayTimeMultiplier());
        this.visibility = delta < displayTime ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, long delta)
    {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, 160, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ControllerLayoutScreen.TEXTURE, 8, 8, 20, 43, 20, 16, 256, 256);
        String title = font.plainSubstrByWidth(this.controllerName.getString(), 120);
        graphics.drawString(font, title, 35, 7, 0xFFFFFFFF, false);
        Component message = this.connected ? LABEL_CONNECTED : LABEL_DISCONNECTED;
        graphics.drawString(font, message, 35, 18, 0xFFFFFF00, false);
    }
}
