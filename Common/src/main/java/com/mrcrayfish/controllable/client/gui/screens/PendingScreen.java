package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class PendingScreen extends Screen
{
    public PendingScreen(Component title)
    {
        super(title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        String loadingBar = switch((int) (Util.getMillis() / 300L % 4L)) {
            case 1, 3 -> "o O o";
            case 2 -> "o o O";
            default -> "O o o";
        };
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        graphics.drawCenteredString(this.font, loadingBar, centerX, centerY - 9, -1);
        graphics.drawCenteredString(this.font, this.title, centerX, centerY + 5, 0x808080);
    }
}