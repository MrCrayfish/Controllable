package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ImageStateButton extends Button
{
    private ResourceLocation texture;
    private int u, v;
    private Supplier<Boolean> state;

    public ImageStateButton(int x, int y, int width, ResourceLocation texture, int u, int v, Supplier<Boolean> state, IPressable onPress)
    {
        super(x, y, width, 20, StringTextComponent.EMPTY, onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.state = state;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture);
        int offset = this.state.get() ? 16 : 0;
        this.blit(matrixStack, this.x + 2, this.y + 2, this.u + offset, this.v, 16, 16);
    }
}
