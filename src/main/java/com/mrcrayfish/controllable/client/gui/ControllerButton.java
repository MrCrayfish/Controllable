package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends AbstractGui
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
        blit(matrixStack, buttonX, buttonY, this.width * this.scale, this.height * this.scale, buttonU, buttonV, this.width, this.height, 256, 256);
        RenderSystem.disableBlend();

        int remappedButton = this.button;

        Map<Integer, Integer> reassignments = this.screen.getReassignments();
        for(Integer key : reassignments.keySet())
        {
            if(reassignments.get(key) == this.button)
            {
                remappedButton = key;
                break;
            }
        }

        // Draws an exclamation if the button has no button assigned to it!
        if(!reassignments.values().contains(this.button) && this.screen.remap(this.button) != this.button)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(ControllerLayoutScreen.TEXTURE);
            blit(matrixStack, buttonX + (buttonWidth - 4) / 2, buttonY + (buttonHeight - 15) / 2, 4, 15, 88, 0, 4, 15, 256, 256);
            return;
        }

        if(!FMLLoader.isProduction())
        {
            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0);
            String mapping = String.valueOf(remappedButton);
            int width = Minecraft.getInstance().fontRenderer.getStringWidth(mapping);
            drawString(matrixStack, Minecraft.getInstance().fontRenderer, mapping, buttonX + (buttonWidth - width) / 2, buttonY + (buttonHeight - 9) / 2, 0xFFFFFFFF);
            matrixStack.pop();
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
}
