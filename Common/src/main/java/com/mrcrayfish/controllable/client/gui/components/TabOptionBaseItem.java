package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public abstract class TabOptionBaseItem extends TabSelectionList.BaseItem
{
    private int labelColor = 0xFFFFFFFF;

    public TabOptionBaseItem(Component label)
    {
        super(label);
    }

    public TabOptionBaseItem setLabel(Component label)
    {
        this.label = label;
        return this;
    }

    public void setLabelColor(int labelColor)
    {
        this.labelColor = labelColor;
    }

    @Override
    public void render(PoseStack poseStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTick)
    {
        // Draws a transparent black background on every odd item to help match the widgets with the label
        if(slotIndex % 2 != 0)
        {
            Screen.fill(poseStack, left - 2, top - 2, left + listWidth + 2, top + slotHeight + 2, 0x55000000);
        }
        if(Controllable.getInput().isControllerInUse() && ScreenUtil.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ScreenUtil.drawOutlinedBox(poseStack, left - 2, top - 2, listWidth + 4, slotHeight + 4, 0xAAFFFFFF);
        }
        Font font = Minecraft.getInstance().font;
        GuiComponent.drawString(poseStack, font, this.label, left + 5, top + (slotHeight - font.lineHeight) / 2 + 1, this.labelColor);
    }

    @Override
    public List<? extends NarratableEntry> narratables()
    {
        return ImmutableList.of(new NarratableEntry()
        {
            @Override
            public NarratableEntry.NarrationPriority narrationPriority()
            {
                return NarratableEntry.NarrationPriority.HOVERED;
            }

            @Override
            public void updateNarration(NarrationElementOutput output)
            {
                output.add(NarratedElementType.TITLE, TabOptionBaseItem.this.label);
            }
        });
    }
}
