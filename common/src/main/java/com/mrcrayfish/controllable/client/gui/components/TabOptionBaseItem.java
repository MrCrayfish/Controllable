package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.navigation.HideCursor;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.framework.api.config.AbstractProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class TabOptionBaseItem extends TabSelectionList.BaseItem implements HideCursor, FilteredItem
{
    private int labelColor = 0xFFFFFFFF;
    protected @Nullable TabOptionBaseItem dependentOption;
    protected boolean invertDependentOption;
    protected Supplier<Boolean> visibilityCondition = () -> true;

    public TabOptionBaseItem(Component label)
    {
        super(label);
    }

    public boolean isEnabled()
    {
        return true;
    }

    protected boolean isOptionActive()
    {
        return this.dependentOption == null || this.invertDependentOption && !this.dependentOption.isEnabled() || !this.invertDependentOption && this.dependentOption.isEnabled();
    }

    public void setDependentOption(@Nullable TabOptionBaseItem required)
    {
        this.dependentOption = required;
    }

    public void setDependentOption(@Nullable TabOptionBaseItem required, boolean invert)
    {
        this.setDependentOption(required);
        this.invertDependentOption = invert;
    }

    public void setVisibilityCondition(Supplier<Boolean> visibilityCondition)
    {
        this.visibilityCondition = visibilityCondition;
    }

    @Override
    public boolean isVisible()
    {
        return this.visibilityCondition.get();
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
    public void render(GuiGraphics graphics, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTick)
    {
        // Draws a transparent black background on every odd item to help match the widgets with the label
        if(slotIndex % 2 != 0)
        {
            graphics.fill(left - 2, top - 2, left + listWidth + 2, top + slotHeight + 2, 0x55000000);
        }
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed() && ScreenHelper.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ScreenHelper.drawOutlinedBox(graphics, left - 2, top - 2, listWidth + 4, slotHeight + 4, 0xAAFFFFFF);
        }
        Font font = Minecraft.getInstance().font;
        int textColor = this.isOptionActive() ? (this.labelColor | 0xFF000000) : 0xFF777777;
        graphics.drawString(font, this.label, left + 5, top + (slotHeight - font.lineHeight) / 2 + 1, textColor);
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

    protected static Component createTooltipMessage(AbstractProperty<?> property)
    {
        String tooltipKey = property.getTranslationKey() + ".tooltip";
        if(I18n.exists(tooltipKey))
        {
            return Component.translatable(tooltipKey);
        }
        return Component.literal(property.getComment());
    }

    protected static Tooltip createTooltipWithWidth(Component message, int width)
    {
        Minecraft mc = Minecraft.getInstance();
        List<FormattedText> lines = mc.font.getSplitter().splitLines(message, width, Style.EMPTY);
        return ClientHelper.createListTooltip(lines);
    }
}
