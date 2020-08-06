package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends AbstractGui
{
    protected int button;
    private int x, y;
    private int u, v;
    private int width, height;
    private int scale;
    private boolean hovered;

    public ControllerButton(int button, int x, int y, int u, int v, int width, int height, int scale)
    {
        this.button = button;
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public void draw(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, boolean selected)
    {
        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
        int buttonU = this.u;
        int buttonV = this.v;
        int buttonX = x + this.x * this.scale;
        int buttonY = y + this.y * this.scale;
        int buttonWidth = this.width * this.scale;
        int buttonHeight = this.height * this.scale;
        this.hovered = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
        if(this.hovered)
        {
            buttonV += this.height * 2;
        }
        else if(Controllable.getController() != null && Controllable.isButtonPressed(this.button) || selected)
        {
            buttonV += this.height;
        }
        blit(matrixStack, buttonX, buttonY, this.width * this.scale, this.height * this.scale, buttonU, buttonV, this.width, this.height, 256, 256);
        RenderSystem.disableBlend();
    }

    public int getButton()
    {
        return this.button;
    }

    public boolean isHovered()
    {
        return this.hovered;
    }
}
