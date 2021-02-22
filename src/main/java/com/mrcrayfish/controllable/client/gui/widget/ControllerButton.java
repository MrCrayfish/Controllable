package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends PressableButton
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    public ControllerButton(int x, int y, Consumer<GuiButton> onPress)
    {
        super(x, y, 20, 20, "", onPress);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int textureV = 43;
        if(mouseOver)
        {
            textureV += this.height;
        }
        this.drawTexturedModalRect(this.x, this.y, 0, textureV, this.width, this.height);
    }
}
