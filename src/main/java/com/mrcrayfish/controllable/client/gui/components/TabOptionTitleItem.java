package com.mrcrayfish.controllable.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
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
    public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
    {
        Font font = Minecraft.getInstance().font;
        int labelWidth = font.width(this.label) + 2;
        ScreenUtil.drawRoundedBox(poseStack, left + width / 2 - labelWidth / 2, top + 2, labelWidth, 14, 0x88000000);
        Screen.drawCenteredString(poseStack, font, this.label, left + width / 2, top + 5, 0xFFFFFF);
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
