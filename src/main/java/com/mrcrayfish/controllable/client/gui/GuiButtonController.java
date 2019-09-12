package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrcrayfish.controllable.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class GuiButtonController extends Button
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    public GuiButtonController(int x, int y, IPressable pressable)
    {
        super(x, y, 20, 20, "", pressable);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
            boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int textureV = 43;
            if (mouseOver)
            {
                textureV += this.height;
            }
            this.blit(this.x, this.y, 0, textureV, this.width, this.height);
        }
    }
}
