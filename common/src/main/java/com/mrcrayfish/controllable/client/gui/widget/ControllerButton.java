package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class ControllerButton extends Button
{
    private static final ResourceLocation TEXTURE = Utils.resource("textures/gui/controller.png");
    private final AbstractWidget widget;

    public ControllerButton(AbstractWidget widget, OnPress onPress)
    {
        super(0, 0, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.widget = widget;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.setX(this.widget.getRight() + 4);
        this.setY(this.widget.getY());
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
