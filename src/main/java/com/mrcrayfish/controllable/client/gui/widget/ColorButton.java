package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Author: MrCrayfish
 */
public class ColorButton extends Button
{
    private static final ChatFormatting[] COLORS = {
        ChatFormatting.BLACK,
        ChatFormatting.DARK_BLUE,
        ChatFormatting.DARK_GREEN,
        ChatFormatting.DARK_AQUA,
        ChatFormatting.DARK_RED,
        ChatFormatting.DARK_PURPLE,
        ChatFormatting.GOLD,
        ChatFormatting.GRAY,
        ChatFormatting.DARK_GRAY,
        ChatFormatting.BLUE,
        ChatFormatting.GREEN,
        ChatFormatting.AQUA,
        ChatFormatting.RED,
        ChatFormatting.LIGHT_PURPLE,
        ChatFormatting.YELLOW,
        ChatFormatting.WHITE
    };

    private int index = 14;

    public ColorButton(int x, int y, OnPress onPress)
    {
        super(x, y, 20, 20, CommonComponents.EMPTY, onPress);
    }

    public void setColor(ChatFormatting color)
    {
        int index = ArrayUtils.indexOf(COLORS, color);
        if(index != -1)
        {
            this.index = index;
        }
    }

    public ChatFormatting getColor()
    {
        return COLORS[this.index];
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        this.index = (this.index + 1) % COLORS.length;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        fill(poseStack, this.x + 4, this.y + 4, this.x + 16, this.y + 16, 0xFF000000);
        fill(poseStack, this.x + 5, this.y + 5, this.x + 15, this.y + 15, COLORS[this.index].getColor() + 0xFF000000);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
    }
}
