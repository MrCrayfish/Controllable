package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends Button
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    public ControllerButton(int x, int y, OnPress onPress)
    {
        super(x, y, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        boolean mouseOver = ScreenUtil.isMouseWithin(mouseX, mouseY, this.getX(), this.getY(), this.width, this.height);
        int textureV = 43;
        if(mouseOver)
        {
            textureV += this.height;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(matrixStack, this.getX(), this.getY(), 0, textureV, this.width, this.height);
    }
}
