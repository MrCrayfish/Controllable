package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Author: MrCrayfish
 */
public class ControllerButton
{
    protected ControllerLayoutScreen screen;
    protected int button;
    private int x, y;
    private int u, v;
    private int width, height;
    private int scale;
    private boolean hovered;

    public ControllerButton(ControllerLayoutScreen screen, int button, int x, int y, int u, int v, int width, int height, int scale)
    {
        this.screen = screen;
        this.button = button;
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public void draw(int x, int y, int mouseX, int mouseY, boolean selected)
    {
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
        int buttonU = this.u;
        int buttonV = this.v;
        int buttonX = x + this.x * this.scale;
        int buttonY = y + this.y * this.scale;
        int buttonWidth = this.width * this.scale;
        int buttonHeight = this.height * this.scale;
        Controller controller = Controllable.getController();
        this.hovered = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
        if(this.hovered)
        {
            buttonV += this.height * 2;
        }
        else if(controller != null && this.screen.isButtonPressed(this.button) || selected)
        {
            buttonV += this.height;
        }
        GuiScreen.drawScaledCustomSizeModalRect(buttonX, buttonY, buttonU, buttonV, this.width, this.height, this.width * this.scale, this.height * this.scale, 256, 256);
        GlStateManager.disableBlend();

        // Draws an exclamation if the button has no button assigned to it!
        if(this.isMissingMapping())
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
            GuiScreen.drawScaledCustomSizeModalRect(buttonX + (buttonWidth - 4) / 2, buttonY + (buttonHeight - 15) / 2, 88, 0, 4, 15, 4, 15, 256, 256);
        }
    }

    public int getButton()
    {
        return this.button;
    }

    public boolean isHovered()
    {
        return this.hovered;
    }

    public boolean isMissingMapping()
    {
        return !this.screen.getReassignments().values().contains(this.button) && this.screen.remap(this.button) != this.button;
    }
}
