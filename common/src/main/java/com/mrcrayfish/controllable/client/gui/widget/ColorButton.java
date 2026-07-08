package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ColorButton extends Button
{
    private static final TextColor[] COLORS = {
        TextColor.BLACK,
        TextColor.DARK_BLUE,
        TextColor.DARK_GREEN,
        TextColor.DARK_AQUA,
        TextColor.DARK_RED,
        TextColor.DARK_PURPLE,
        TextColor.GOLD,
        TextColor.GRAY,
        TextColor.DARK_GRAY,
        TextColor.BLUE,
        TextColor.GREEN,
        TextColor.AQUA,
        TextColor.RED,
        TextColor.LIGHT_PURPLE,
        TextColor.YELLOW,
        TextColor.WHITE
    };

    private int index = 14;

    public ColorButton(int x, int y, OnPress onPress)
    {
        super(x, y, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
    }

    public void setColor(TextColor color)
    {
        int index = ArrayUtils.indexOf(COLORS, color);
        if(index != -1)
        {
            this.index = index;
        }
    }

    public TextColor getColor()
    {
        return COLORS[this.index];
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick)
    {
        this.index = (this.index + 1) % COLORS.length;
        super.onClick(event, doubleClick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks)
    {
        this.extractDefaultSprite(extractor);
        extractor.fill(this.getX() + 4, this.getY() + 4, this.getX() + 16, this.getY() + 16, 0xFF000000);
        extractor.fill(this.getX() + 5, this.getY() + 5, this.getX() + 15, this.getY() + 15, COLORS[this.index].getValue() + 0xFF000000);
    }
}
