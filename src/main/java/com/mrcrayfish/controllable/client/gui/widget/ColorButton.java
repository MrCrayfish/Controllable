package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Author: MrCrayfish
 */
public class ColorButton extends Button
{
    private static final TextFormatting[] COLORS = {
        TextFormatting.BLACK,
        TextFormatting.DARK_BLUE,
        TextFormatting.DARK_GREEN,
        TextFormatting.DARK_AQUA,
        TextFormatting.DARK_RED,
        TextFormatting.DARK_PURPLE,
        TextFormatting.GOLD,
        TextFormatting.GRAY,
        TextFormatting.DARK_GRAY,
        TextFormatting.BLUE,
        TextFormatting.GREEN,
        TextFormatting.AQUA,
        TextFormatting.RED,
        TextFormatting.LIGHT_PURPLE,
        TextFormatting.YELLOW,
        TextFormatting.WHITE
    };

    private int index = 14;

    public ColorButton(int x, int y, IPressable onPress)
    {
        super(x, y, 20, 20, StringTextComponent.EMPTY, onPress);
    }

    public void setColor(TextFormatting color)
    {
        int index = ArrayUtils.indexOf(COLORS, color);
        if(index != -1)
        {
            this.index = index;
        }
    }

    public TextFormatting getColor()
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
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        fill(matrixStack, this.x + 4, this.y + 4, this.x + 16, this.y + 16, 0xFF000000);
        fill(matrixStack, this.x + 5, this.y + 5, this.x + 15, this.y + 15, COLORS[this.index].getColor() + 0xFF000000);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
    }
}
