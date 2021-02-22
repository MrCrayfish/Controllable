package com.mrcrayfish.controllable.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ImageButton extends PressableButton
{
    private ResourceLocation texture;
    private int imageU, imageV;
    private int imageWidth, imageHeight;

    public ImageButton(int x, int y, int width, ResourceLocation texture, int imageU, int imageV, int imageWidth, int imageHeight, Consumer<GuiButton> onPress)
    {
        super(x, y, width, 20, "", onPress);
        this.texture = texture;
        this.imageU = imageU;
        this.imageV = imageV;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        if(!this.enabled) GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        this.drawTexturedModalRect(this.x + (this.width - this.imageWidth) / 2, this.y + 2, this.imageU, this.imageV, this.imageWidth, this.imageHeight);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
