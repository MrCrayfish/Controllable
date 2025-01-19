package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class BackgroundStringWidget extends AbstractStringWidget
{
    public BackgroundStringWidget(Component message, Font font)
    {
        this(0, 0, message, font);
    }

    public BackgroundStringWidget(int x, int y, Component message, Font font)
    {
        super(x, y, font.width(message.getVisualOrderText()) + 4, font.lineHeight + 4, message, font);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        ScreenHelper.drawRoundedBox(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0x55000000);
        graphics.drawString(this.getFont(), this.getMessage().getVisualOrderText(), this.getX() + 2, this.getY() + 2, this.getColor());
    }
}
