package com.mrcrayfish.controllable.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Author: MrCrayfish
 */
public class ImageButton extends Button
{
    private final Identifier texture;
    private final int imageU, imageV;
    private final int imageWidth, imageHeight;
    private final int textureWidth, textureHeight;

    public ImageButton(int x, int y, int width, Identifier texture, int imageU, int imageV, int imageWidth, int imageHeight, OnPress onPress)
    {
        this(x, y, width, texture, imageU, imageV, imageWidth, imageHeight, 256, 256, onPress);
    }

    public ImageButton(int x, int y, int width, Identifier texture, int imageU, int imageV, int imageWidth, int imageHeight, int textureWidth, int textureHeight, OnPress onPress)
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
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.extractDefaultSprite(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX() + (this.width - this.imageWidth) / 2, this.getY() + (this.height - this.imageHeight) / 2, this.imageU, this.imageV, this.imageWidth, this.imageHeight, this.textureWidth, this.textureHeight, !this.active ? 0x88FFFFFF : 0xFFFFFFFF);
    }
}
