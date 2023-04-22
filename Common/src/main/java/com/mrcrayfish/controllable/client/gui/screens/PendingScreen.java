package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        String loadingBar = switch((int) (Util.getMillis() / 300L % 4L)) {
            case 1, 3 -> "o O o";
            case 2 -> "o o O";
            default -> "O o o";
        };
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        drawCenteredString(poseStack, this.font, loadingBar, centerX, centerY - 9, -1);
        drawCenteredString(poseStack, this.font, this.title, centerX, centerY + 5, 0x808080);
    }
}