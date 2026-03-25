package com.mrcrayfish.controllable.client.gui.components;

import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TabOptionTitleItem extends TabOptionBaseItem implements SkipItem
{
    public TabOptionTitleItem(Component label)
    {
        super(label);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick)
    {
        Font font = Minecraft.getInstance().font;
        int labelWidth = font.width(this.label) + 2;
        ScreenHelper.drawRoundedBox(graphics, this.getX() + this.getWidth() / 2 - labelWidth / 2, this.getY() + 4, labelWidth, 14, 0x88000000);
        graphics.centeredText(font, this.label, this.getX() + this.getWidth() / 2, this.getY() + 7, 0xFFFFFFFF);
    }

    @Override
    public List<? extends NarratableEntry> narratables()
    {
        return Collections.emptyList();
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return Collections.emptyList();
    }
}
