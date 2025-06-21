package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

/**
 * Author: MrCrayfish
 */
public class ImageButton extends Button
{
    private final ResourceLocation texture;
    private final int imageU, imageV;
    private final int imageWidth, imageHeight;
    private final int textureWidth, textureHeight;

    public ImageButton(int x, int y, int width, ResourceLocation texture, int imageU, int imageV, int imageWidth, int imageHeight, OnPress onPress)
    {
        this(x, y, width, texture, imageU, imageV, imageWidth, imageHeight, 256, 256, onPress);
    }

    public ImageButton(int x, int y, int width, ResourceLocation texture, int imageU, int imageV, int imageWidth, int imageHeight, int textureWidth, int textureHeight, OnPress onPress)
    {
        super(x, y, width, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.imageU = imageU;
        this.imageV = imageV;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX() + (this.width - this.imageWidth) / 2, this.getY() + (this.height - this.imageHeight) / 2, this.imageU, this.imageV, this.imageWidth, this.imageHeight, this.textureWidth, this.textureHeight, !this.active ? ARGB.white(0.5F) : ARGB.white(1));
    }
}
