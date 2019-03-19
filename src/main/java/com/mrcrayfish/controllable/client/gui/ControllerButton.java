package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends Gui
{
    protected int button;
    private int x, y;
    private int u, v;
    private int width, height;
    private int scale;

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

    public void draw(int x, int y, int mouseX, int mouseY)
    {
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiControllerLayout.TEXTURE);
        int buttonV = v;
        int buttonX = x + this.x * this.scale;
        int buttonY = y + this.y * this.scale;
        int buttonWidth = this.width * this.scale;
        int buttonHeight = this.height * this.scale;
        boolean mouseOver = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
        if(mouseOver)
        {
            buttonV += this.height * 2;
        }
        else if(Controllable.getController() != null && Controllable.getController().isButtonPressed(button))
        {
            buttonV += this.height;
        }
        drawScaledCustomSizeModalRect(buttonX, buttonY, u, buttonV, this.width, this.height, this.width * this.scale, this.height * this.scale, 256, 256);
        GlStateManager.disableBlend();
    }
}
