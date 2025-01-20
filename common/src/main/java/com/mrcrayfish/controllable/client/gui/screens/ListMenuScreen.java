package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.gui.ISearchable;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.gui.widget.BackgroundStringWidget;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public abstract class ListMenuScreen extends Screen
{
    protected final Screen parent;
    protected final int itemHeight;
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private List<Item> items = new ArrayList<>();
    protected EntryList list;
    protected FocusedEditBox activeTextField;
    protected FocusedEditBox searchTextField;
    protected Component subTitle;
    protected boolean searchable = true;
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

    public void setSearchable(boolean visible)
    {
        this.searchable = visible;
    }

    public void setRowWidth(int rowWidth)
    {
        this.rowWidth = rowWidth;
    }

    @Override
    protected void init()
    {
        // Constructs a list of entries and adds them to an option list
        this.items = this.constructEntries();
        this.list = new EntryList(this.items);
        this.layout.addToContents(this.list);

        LinearLayout headerLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
        headerLayout.defaultCellSetting().alignHorizontallyCenter();
        this.setupHeader(headerLayout);

        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(4));
        footerLayout.defaultCellSetting().alignVerticallyMiddle();
        this.setupFooter(footerLayout);

        // Set the height according the layout height
        headerLayout.arrangeElements();
        this.layout.setHeaderHeight(headerLayout.getHeight() + 12);

        footerLayout.arrangeElements();
        this.layout.setFooterHeight(footerLayout.getHeight() + 11);

        this.updateSearchTextFieldSuggestion("");
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void setupHeader(LinearLayout headerLayout)
    {
        headerLayout.addChild(new StringWidget(this.getTitle(), this.font));
        if(this.subTitle != null)
        {
            headerLayout.addChild(new BackgroundStringWidget(this.subTitle, this.font));
        }
        if(this.searchable)
        {
            // Adds a search text field to the top of the screen
            this.searchTextField = headerLayout.addChild(new FocusedEditBox(this.font, 0, 0, 220, 20, Component.literal("Search")));
            this.searchTextField.setResponder(s -> {
                this.updateSearchTextFieldSuggestion(s);
                this.filterItems(s);
            });
        }
    }

    private void filterItems(String s)
    {
        this.list.replaceEntries(s.isEmpty() ? this.items : this.items.stream()
            .filter(item -> item instanceof ISearchable searchable && searchable.getLabel()
                .getString()
                .toLowerCase(Locale.ENGLISH)
                .contains(s.toLowerCase(Locale.ENGLISH)))
            .collect(Collectors.toList()));
        if(!s.isEmpty())
        {
            this.list.setScrollAmount(0);
        }
    }

    protected void rebuildItems()
    {
        this.items = this.constructEntries();
        this.filterItems(this.searchTextField.getValue());
    }

    protected void setupFooter(LinearLayout footerLayout) {}

    @Override
    protected void repositionElements()
    {
        this.layout.arrangeElements();
        this.list.updateSize(this.width, this.layout);
    }

    protected abstract List<Item> constructEntries();

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
        public EntryList(List<Item> items)
        {
            // ListMenuScreen.this.height - 44 TODO test
            super(Objects.requireNonNull(ListMenuScreen.this.minecraft), 0, 0, 0, ListMenuScreen.this.itemHeight);
            items.forEach(this::addEntry);
        }

        @Override
        protected void setRenderHeader(boolean p_93474_, int p_93475_)
        {
            super.setRenderHeader(p_93474_, p_93475_);
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
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
        {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            this.renderToolTips(graphics, mouseX, mouseY); // TODO test
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

    public class TitleItem extends Item implements SkipItem
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
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            Font font = Minecraft.getInstance().font;
            int labelWidth = font.width(this.label) + 2;
            ScreenHelper.drawRoundedBox(graphics, left + width / 2 - labelWidth / 2, top + 2, labelWidth, 14, 0x88000000);
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
        if(this.searchTextField == null)
            return;

        if(!value.isEmpty())
        {
            Optional<? extends ISearchable> optional = this.list.children().stream()
                    .filter(item -> item instanceof ISearchable)
                    .map(item -> (ISearchable) item)
                    .filter(item -> item.getLabel().getString().toLowerCase(Locale.ENGLISH).contains(value.toLowerCase(Locale.ENGLISH)))
                    .min(Comparator.comparing(item -> item.getLabel().getString()));
            if(optional.isPresent())
            {
                String displayName = optional.get().getLabel().getString();
                this.searchTextField.setSuggestion(" (%s)".formatted(displayName));
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
