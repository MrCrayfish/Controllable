package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

/**
 * Author: MrCrayfish
 */
public class ImageButton extends Button
{
    private ResourceLocation texture;
    private int imageU, imageV;
    private int imageWidth, imageHeight;

    public ImageButton(int x, int y, int width, ResourceLocation texture, int imageU, int imageV, int imageWidth, int imageHeight, IPressable onPress)
    {
        super(x, y, width, 20, StringTextComponent.EMPTY, onPress);
        this.texture = texture;
        this.imageU = imageU;
        this.imageV = imageV;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture);
        if(!this.active) RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(matrixStack, this.x + (this.width - this.imageWidth) / 2, this.y + 2, this.imageU, this.imageV, this.imageWidth, this.imageHeight);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
