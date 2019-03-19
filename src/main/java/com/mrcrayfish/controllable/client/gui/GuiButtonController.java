package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class GuiButtonController extends GuiButton
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    public GuiButtonController(int buttonId, int x, int y)
    {
        super(buttonId, x, y, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(TEXTURE);
            boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int textureV = 43;
            if (mouseOver)
            {
                textureV += this.height;
            }
            this.drawTexturedModalRect(this.x, this.y, 0, textureV, this.width, this.height);
        }
    }
}
