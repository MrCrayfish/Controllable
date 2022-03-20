package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public abstract class ListMenuScreen extends Screen
{
    protected final Screen parent;
    protected final int itemHeight;
    protected EntryList list;
    protected List<Item> entries;
    protected List<IReorderingProcessor> activeTooltip;
    protected FocusedTextFieldWidget activeTextField;
    protected FocusedTextFieldWidget searchTextField;
    protected ITextComponent subTitle;

    protected ListMenuScreen(Screen parent, ITextComponent title, int itemHeight)
    {
        super(title);
        this.parent = parent;
        this.itemHeight = itemHeight;
    }

    public void setSubTitle(ITextComponent subTitle)
    {
        this.subTitle = subTitle;
    }

    @Override
    protected void init()
    {
        // Constructs a list of entries and adds them to an option list
        List<Item> entries = new ArrayList<>();
        this.constructEntries(entries);
        this.entries = ImmutableList.copyOf(entries); //Should this still be immutable?
        this.list = new EntryList(this.entries, this.subTitle != null);
        this.list.func_244605_b(!isPlayingGame());
        this.children.add(this.list);

        // Adds a search text field to the top of the screen
        this.searchTextField = new FocusedTextFieldWidget(this.font, this.width / 2 - 110, this.subTitle != null ? 36 : 22, 220, 20, new StringTextComponent("Search"));
        this.searchTextField.setResponder(s ->
        {
            ScreenUtil.updateSearchTextFieldSuggestion(this.searchTextField, s, this.entries);
            this.list.replaceEntries(s.isEmpty() ? this.entries : this.entries.stream().filter(item -> {
                return !(item instanceof IIgnoreSearch) && item.getLabel().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
            }).collect(Collectors.toList()));
            if(!s.isEmpty())
            {
                this.list.setScrollAmount(0);
            }
        });
        this.children.add(this.searchTextField);
        ScreenUtil.updateSearchTextFieldSuggestion(this.searchTextField, "", this.entries);
    }

    protected abstract void constructEntries(List<Item> entries);

    /**
     * Sets the tool tip to render. Must be actively called in the render method as
     * the tooltip is reset every draw call.
     *
     * @param tooltip a tooltip list to show
     */
    public void setActiveTooltip(List<IReorderingProcessor> tooltip)
    {
        this.activeTooltip = tooltip;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        // Resets the active tooltip each draw call
        this.activeTooltip = null;

        // Draws the background texture (dirt or custom texture)
        this.renderBackground(matrixStack);

        // Draws widgets manually since they are not buttons
        this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        this.searchTextField.render(matrixStack, mouseX, mouseY, partialTicks);

        // Draw title
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 7, 0xFFFFFF);

        // Draw sub title
        if(this.subTitle != null)
        {
            drawCenteredString(matrixStack, this.font, this.subTitle, this.width / 2, 21, 0xFFFFFF);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Draws the active tooltip otherwise tries to draw button tooltips
        if(this.activeTooltip != null)
        {
            this.renderTooltip(matrixStack, this.activeTooltip, mouseX, mouseY);
        }
        else
        {
            for(IGuiEventListener widget : this.getEventListeners())
            {
                if(widget instanceof Button && ((Button) widget).isHovered())
                {
                    ((Button) widget).renderToolTip(matrixStack, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(ScreenUtil.isMouseWithin(10, 13, 23, 23, (int) mouseX, (int) mouseY))
        {
            Style style = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured"));
            this.handleComponentClicked(style);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static boolean isPlayingGame()
    {
        return Minecraft.getInstance().player != null;
    }

    protected class EntryList extends AbstractOptionList<Item>
    {
        public EntryList(List<Item> entries, boolean extended)
        {
            super(ListMenuScreen.this.minecraft, ListMenuScreen.this.width, ListMenuScreen.this.height, extended ? 64 : 50, ListMenuScreen.this.height - 44, ListMenuScreen.this.itemHeight);
            entries.forEach(this::addEntry);
        }

        @Override
        protected int getScrollbarPosition()
        {
            return this.width / 2 + 134;
        }

        @Override
        public int getRowWidth()
        {
            return 240;
        }

        // Overridden simply to make it public
        @Override
        public void replaceEntries(Collection<Item> entries)
        {
            super.replaceEntries(entries);
        }

        @Override
        public boolean removeEntry(Item item)
        {
            return super.removeEntry(item);
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            this.renderToolTips(matrixStack, mouseX, mouseY);
        }

        private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY)
        {
            if(this.isMouseOver(mouseX, mouseY) && mouseX < ListMenuScreen.this.list.getRowLeft() + ListMenuScreen.this.list.getRowWidth() - 67)
            {
                Item item = this.getEntryAtPosition(mouseX, mouseY);
                if(item != null)
                {
                    ListMenuScreen.this.setActiveTooltip(item.tooltip);
                }
            }
            this.getEventListeners().forEach(item ->
            {
                item.getEventListeners().forEach(o ->
                {
                    if(o instanceof Button)
                    {
                        ((Button) o).renderToolTip(matrixStack, mouseX, mouseY);
                    }
                });
            });
        }
    }

    protected abstract class Item extends AbstractOptionList.Entry<Item> implements ISearchable
    {
        protected final ITextComponent label;
        protected List<IReorderingProcessor> tooltip;

        public Item(ITextComponent label)
        {
            this.label = label;
        }

        public Item(String label)
        {
            this.label = new StringTextComponent(label);
        }

        @Override
        public String getLabel()
        {
            return this.label.getUnformattedComponentText();
        }

        public void setTooltip(ITextComponent text, int maxWidth)
        {
            this.tooltip = ListMenuScreen.this.minecraft.fontRenderer.trimStringToWidth(text, maxWidth);
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return Collections.emptyList();
        }
    }

    public class TitleItem extends Item implements IIgnoreSearch
    {
        public TitleItem(ITextComponent title)
        {
            super(title);
        }

        public TitleItem(String title)
        {
            super(new StringTextComponent(title).mergeStyle(TextFormatting.BOLD).mergeStyle(TextFormatting.YELLOW));
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            AbstractGui.drawCenteredString(matrixStack, ListMenuScreen.this.minecraft.fontRenderer, this.label, left + width / 2, top + 5, 0xFFFFFF);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected class FocusedTextFieldWidget extends TextFieldWidget
    {
        public FocusedTextFieldWidget(FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent label)
        {
            super(fontRenderer, x, y, width, height, label);
        }

        @Override
        public void setFocused2(boolean focused)
        {
            super.setFocused2(focused);
            if(focused)
            {
                if(ListMenuScreen.this.activeTextField != null && ListMenuScreen.this.activeTextField != this)
                {
                    ListMenuScreen.this.activeTextField.setFocused2(false);
                }
                ListMenuScreen.this.activeTextField = this;
            }
        }
    }

    protected interface IIgnoreSearch {}
}