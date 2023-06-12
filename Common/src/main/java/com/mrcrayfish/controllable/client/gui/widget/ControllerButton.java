package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends Button
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/controller.png");

    public ControllerButton(int x, int y, OnPress onPress)
    {
        super(x, y, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        boolean mouseOver = ScreenHelper.isMouseWithin(mouseX, mouseY, this.getX(), this.getY(), this.width, this.height);
        int textureV = 43;
        if(mouseOver)
        {
            textureV += this.height;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, this.getX(), this.getY(), 0, textureV, this.width, this.height);
    }
}
