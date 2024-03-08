package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.gui.ISearchable;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
    protected FocusedEditBox activeTextField;
    protected FocusedEditBox searchTextField;
    protected Component subTitle;
    protected boolean searchBarVisible = true;
    protected int rowWidth = 240;

    protected ListMenuScreen(Screen parent, Component title, int itemHeight)
    {
        super(title);
        this.parent = parent;
        this.itemHeight = itemHeight;
    }

    public void setSubTitle(Component subTitle)
    {
        this.subTitle = subTitle;
    }

    public void setSearchBarVisible(boolean visible)
    {
        this.searchBarVisible = visible;
    }

    public void setRowWidth(int rowWidth)
    {
        this.rowWidth = rowWidth;
    }

    @Override
    protected void init()
    {
        // Constructs a list of entries and adds them to an option list
        List<Item> entries = new ArrayList<>();
        this.constructEntries(entries);
        this.entries = ImmutableList.copyOf(entries); //Should this still be immutable?
        this.list = new EntryList(this.entries, this.calculateTop());
        this.list.setRenderBackground(!ClientHelper.isPlayingGame());
        this.addWidget(this.list);

        // Adds a search text field to the top of the screen
        this.searchTextField = new FocusedEditBox(this.font, this.width / 2 - 110, this.calculateSearchBarY(), 220, 20, Component.literal("Search"));
        this.searchTextField.setResponder(s ->
        {
            this.updateSearchTextFieldSuggestion(s);
            this.list.replaceEntries(s.isEmpty() ? this.entries : this.entries.stream().filter(item -> {
                return item instanceof ISearchable searchable && searchable.getLabel().plainCopy().toString().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
            }).collect(Collectors.toList()));
            if(!s.isEmpty())
            {
                this.list.setScrollAmount(0);
            }
        });
        this.addWidget(this.searchTextField);
        this.searchTextField.visible = this.searchBarVisible;
        this.updateSearchTextFieldSuggestion("");
    }

    private int calculateTop()
    {
        int top = 30;
        if(this.searchBarVisible)
        {
            top += 20;
        }
        if(this.subTitle != null)
        {
            top += 14;
        }
        return top;
    }

    private int calculateSearchBarY()
    {
        return this.subTitle != null ? 36 : 22;
    }

    protected abstract void constructEntries(List<Item> entries);

    /**
     * Sets the tool tip to render. Must be actively called in the render method as
     * the tooltip is reset every draw call.
     *
     * @param tooltip a tooltip list to show
     */
    public void setActiveTooltip(@Nullable List<FormattedCharSequence> tooltip)
    {
        if(tooltip != null)
        {
            this.setTooltipForNextRenderPass(tooltip);
        }
    }

    protected void updateTooltip(int mouseX, int mouseY)
    {
        if(ScreenHelper.isMouseWithin(10, 13, 23, 23, mouseX, mouseY))
        {
            this.setActiveTooltip(Objects.requireNonNull(this.minecraft).font.split(Component.translatable("configured.gui.info"), 200));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        // Draws the background texture (dirt or custom texture)
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        // Draws widgets manually since they are not buttons
        this.list.render(graphics, mouseX, mouseY, partialTicks);
        this.searchTextField.render(graphics, mouseX, mouseY, partialTicks);

        // Draw title
        int titleY = 7 + (!this.searchBarVisible && this.subTitle == null ? 5 : 0);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);

        // Draw sub title
        if(this.subTitle != null)
        {
            graphics.drawCenteredString(this.font, this.subTitle, this.width / 2, 21, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Gives a chance for child classes to set the active tooltip
        this.updateTooltip(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(ScreenHelper.isMouseWithin(10, 13, 23, 23, (int) mouseX, (int) mouseY))
        {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured"));
            this.handleComponentClicked(style);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected class EntryList extends ContainerObjectSelectionList<Item>
    {
        public EntryList(List<Item> entries, int top)
        {
            super(Objects.requireNonNull(ListMenuScreen.this.minecraft), ListMenuScreen.this.width, ListMenuScreen.this.height, top, ListMenuScreen.this.itemHeight);
            entries.forEach(this::addEntry);
        }

        @Override
        protected int getScrollbarPosition()
        {
            return this.width / 2 + ListMenuScreen.this.rowWidth / 2 + 10;
        }

        @Override
        public int getRowWidth()
        {
            return ListMenuScreen.this.rowWidth;
        }

        // Overridden simply to make it public
        @Override
        public void replaceEntries(Collection<Item> entries)
        {
            super.replaceEntries(entries);
        }

        // Overridden simply to make it public
        @Override
        public boolean removeEntry(Item item)
        {
            return super.removeEntry(item);
        }

        @Nullable
        @Override
        public Item getHovered()
        {
            return super.getHovered();
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
        {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);
            this.renderToolTips(graphics, mouseX, mouseY);
        }

        private void renderToolTips(GuiGraphics graphics, int mouseX, int mouseY)
        {
            if(this.isMouseOver(mouseX, mouseY) && mouseX < ListMenuScreen.this.list.getRowLeft() + ListMenuScreen.this.list.getRowWidth() - 67)
            {
                Item item = this.getEntryAtPosition(mouseX, mouseY);
                if(item != null)
                {
                    ListMenuScreen.this.setActiveTooltip(item.tooltip);
                }
            }
            this.children().forEach(item ->
            {
                item.children().forEach(o ->
                {
                    if(o instanceof Button)
                    {
                        //TODO figure out tooltips?
                        //((Button) o).renderToolTip(poseStack, mouseX, mouseY);
                    }
                });
            });
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button)
        {
            boolean wasDragging = this.isDragging();
            this.setDragging(false);
            if(wasDragging && this.getFocused() != null)
            {
                return this.getFocused().mouseReleased(mouseX, mouseY, button);
            }
            return false;
        }
    }

    protected abstract class Item extends ContainerObjectSelectionList.Entry<Item>
    {
        protected final Component label;
        protected List<FormattedCharSequence> tooltip;

        public Item(Component label)
        {
            this.label = label;
        }

        public Item(String label)
        {
            this.label = Component.literal(label);
        }

        public void setTooltip(Component text, int maxWidth)
        {
            this.tooltip = Objects.requireNonNull(ListMenuScreen.this.minecraft).font.split(text, maxWidth);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables()
        {
            return ImmutableList.of(new NarratableEntry()
            {
                @Override
                public NarrationPriority narrationPriority()
                {
                    return NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output)
                {
                    output.add(NarratedElementType.TITLE, label);
                }
            });
        }
    }

    public class TitleItem extends Item
    {
        public TitleItem(Component title)
        {
            super(title);
        }

        public TitleItem(String title)
        {
            super(Component.literal(title).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW));
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            graphics.drawCenteredString(Objects.requireNonNull(ListMenuScreen.this.minecraft).font, this.label, left + width / 2, top + 5, 0xFFFFFF);
        }
    }

    protected class FocusedEditBox extends EditBox
    {
        public FocusedEditBox(Font font, int x, int y, int width, int height, Component label)
        {
            super(font, x, y, width, height, label);
        }

        @Override
        public void setFocused(boolean focused)
        {
            super.setFocused(focused);
            if(focused)
            {
                if(ListMenuScreen.this.activeTextField != null && ListMenuScreen.this.activeTextField != this)
                {
                    ListMenuScreen.this.activeTextField.setFocused(false);
                }
                ListMenuScreen.this.activeTextField = this;
            }
        }
    }

    protected void updateSearchTextFieldSuggestion(String value)
    {
        if(!value.isEmpty())
        {
            Optional<? extends ISearchable> optional = this.entries.stream()
                    .filter(item -> item instanceof ISearchable)
                    .map(item -> (ISearchable) item)
                    .filter(item -> item.getLabel().plainCopy().toString().toLowerCase(Locale.ENGLISH).startsWith(value.toLowerCase(Locale.ENGLISH)))
                    .min(Comparator.comparing(item -> item.getLabel().plainCopy().toString()));
            if(optional.isPresent())
            {
                int length = value.length();
                String displayName = optional.get().getLabel().plainCopy().toString();
                this.searchTextField.setSuggestion(displayName.substring(length));
            }
            else
            {
                this.searchTextField.setSuggestion("");
            }
        }
        else
        {
            this.searchTextField.setSuggestion(Component.translatable("controllable.gui.search").getString());
        }
    }
}
