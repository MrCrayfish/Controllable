package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class ImageButton extends Button
{
    private ResourceLocation texture;
    private int imageU, imageV;
    private int imageWidth, imageHeight;

    public ImageButton(int x, int y, int width, ResourceLocation texture, int imageU, int imageV, int imageWidth, int imageHeight, OnPress onPress)
    {
        super(x, y, width, 20, CommonComponents.EMPTY, onPress);
        this.texture = texture;
        this.imageU = imageU;
        this.imageV = imageV;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if(!this.active) RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(poseStack, this.x + (this.width - this.imageWidth) / 2, this.y + (this.height - this.imageHeight) / 2, this.imageU, this.imageV, this.imageWidth, this.imageHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
