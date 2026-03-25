package com.mrcrayfish.controllable.client.gui.components;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.input.MultiController;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ControllerList extends TabSelectionList<ControllerList.ControllerEntry>
{
    private static final Identifier CHECKMARK = Identifier.withDefaultNamespace("container/beacon/confirm");

    private final AdaptiveControllerManager manager;
    private final MutableComponent footerSubText;
    private final Screen holder;
    private int controllerCount;

    public ControllerList(Screen holder, Minecraft mc, int itemHeight)
    {
        super(mc, itemHeight);
        this.holder = holder;
        this.manager = Controllable.getControllerManager();
        this.setHeaderText(Component.translatable("controllable.gui.title.select_controller").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
        this.footerSubText = Component.translatable("controllable.gui.controller_missing_2").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
        this.footerSubText.setStyle(this.footerSubText.getStyle().withClickEvent(new ClickEvent.OpenUrl(URI.create("https://mrcrayfish.gitbook.io/controllable-documentation/"))));
        this.setFooterText(Component.translatable("controllable.gui.controller_missing", this.footerSubText));
        this.reloadControllers();
    }

    private void reloadControllers()
    {
        this.clearEntries();
        Map<Number, Pair<Integer, String>> controllers = this.manager.getControllers();
        controllers.forEach((jid, pair) -> this.addEntry(new ControllerEntry(jid, pair.getLeft(), pair.getRight())));
        this.updateSelected();
    }

    private void updateSelected()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            this.setSelected(null);
            return;
        }

        List<ControllerEntry> entries = this.children();
        for(ControllerEntry entry : entries)
        {
            if(Objects.equals(entry.getJid(), controller.getJid()))
            {
                this.setSelected(entry);
                break;
            }
        }
    }

    public void tick()
    {
        if(this.controllerCount != this.manager.getControllerCount())
        {
            this.controllerCount = this.manager.getControllerCount();
            this.reloadControllers();
        }
        this.updateSelected();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        if(this.footerText != null)
        {
            Font font = this.minecraft.font;
            int footerWidth = font.width(this.footerText);
            int footerSubWidth = font.width(this.footerSubText);
            if(ScreenHelper.isMouseWithin(this.getX() + (this.width + footerWidth) / 2 - footerSubWidth, this.getBottom() + 4, footerSubWidth, 14, (int) event.x(), (int) event.y()))
            {
                ClickEvent e = this.footerSubText.getStyle().getClickEvent();
                SettingsScreen.handleClickEvent(e, this.minecraft, this.minecraft.screen);
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractListItems(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick)
    {
        super.extractListItems(extractor, mouseX, mouseY, partialTick);
        this.extractLinkedItems(extractor, mouseX, mouseY);
    }

    private void extractLinkedItems(GuiGraphicsExtractor extractor, int mouseX, int mouseY)
    {
        Controller controller = Controllable.getController();
        if(controller instanceof MultiController multi)
        {
            Set<Number> jids = multi.getControllers().stream().map(Controller::getJid).collect(Collectors.toSet());
            Set<Integer> matchedEntries = new HashSet<>();
            int start = -1, end = -1;
            for(int i = 0; i < this.getItemCount(); i++)
            {
                ControllerEntry entry = this.children().get(i);
                if(jids.contains(entry.getJid()))
                {
                    matchedEntries.add(i);
                    if(start == -1)
                    {
                        start = i;
                    }
                    end = i;
                }
            }
            if(start != end)
            {
                int itemCenter = (this.defaultEntryHeight) / 2 - 1;
                int rowLeft = this.getRowLeft();
                int lineTop = this.getRowTop(start) + itemCenter;
                int lineEnd = this.getRowTop(end) + itemCenter;
                extractor.fill(rowLeft - 12, lineTop, rowLeft - 10, lineEnd, 0xFFFFFFFF);

                int iconTop = lineTop + (lineEnd - lineTop) / 2 - 7;
                int iconLeft = rowLeft - 30;
                extractor.blit(RenderPipelines.GUI_TEXTURED, Icons.TEXTURE, iconLeft, iconTop, 110, 0, 14, 14, 11, 11, Icons.TEXTURE_WIDTH, Icons.TEXTURE_HEIGHT);

                for(int i : matchedEntries)
                {
                    int rowTop = this.getRowTop(i);
                    extractor.fill(rowLeft - 11, rowTop + itemCenter - 1, rowLeft - 4, rowTop + itemCenter + 1, 0xFFFFFFFF);
                }

                if(ScreenHelper.isMouseWithin(iconLeft, iconTop, 14, 14, mouseX, mouseY))
                {
                    extractor.setTooltipForNextFrame(this.createLinkTooltip(), mouseX, mouseY);
                }
            }
        }
    }

    private List<FormattedCharSequence> createLinkTooltip()
    {
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.add(Component.translatable("controllable.gui.linked_controllers").withStyle(ChatFormatting.AQUA).getVisualOrderText());
        lines.addAll(this.minecraft.font.split(Component.translatable("controllable.gui.linked_controllers.desc"), 200));
        return lines;
    }

    public class ControllerEntry extends TabSelectionList.Item<ControllerEntry>
    {
        private final Number jid;
        private final int deviceIndex;

        public ControllerEntry(Number jid, int deviceIndex, String name)
        {
            super(Component.literal(name));
            this.jid = jid;
            this.deviceIndex = deviceIndex;
        }

        public Number getJid()
        {
            return this.jid;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean hovered, float partialTick)
        {
            SelectedState state = this.getSelectedState();
            if(state != SelectedState.NONE)
            {
                ScreenHelper.drawRoundedBox(extractor, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFFFFFFFF);
                ScreenHelper.drawRoundedBox(extractor, this.getX() + 1, this.getY() + 1, this.getWidth() - 2, this.getHeight() - 2, 0xFF000000);
                extractor.blitSprite(RenderPipelines.GUI_TEXTURED, CHECKMARK, this.getX() + 2, this.getY() + 2, 18, 18);
            }
            else if(Controllable.getController() != null && hovered)
            {
                extractor.blit(RenderPipelines.GUI_TEXTURED, Icons.TEXTURE, this.getX() + 4, this.getY() + 6, 110, 0, 11, 11, 11, 11, Icons.TEXTURE_WIDTH, Icons.TEXTURE_HEIGHT);
                extractor.setTooltipForNextFrame(Component.translatable("controllable.gui.link").withStyle(ChatFormatting.AQUA), mouseX, mouseY);
            }
            Font font = Minecraft.getInstance().font;
            extractor.text(font, this.label, this.getX() + 22, this.getY() + (this.getHeight() - font.lineHeight) / 2 + 1, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
        {
            if(event.button() == GLFW.GLFW_MOUSE_BUTTON_1)
            {
                this.connect();
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.75F));
                return true;
            }
            return false;
        }

        private void connect()
        {
            Controller controller = this.getController();
            if(controller == null)
            {
                controller = manager.createController(this.deviceIndex, this.jid);
                if(!manager.addActiveController(controller))
                {
                    // TODO toast
                }
            }
            else if(!manager.removeActiveController(controller))
            {
                // TODO toast
            }
            manager.updateLastDevices();
        }

        private SelectedState getSelectedState()
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                if(controller.getJid().equals(this.jid))
                    return SelectedState.SELECTED;

                if(controller instanceof MultiController m)
                {
                    if(m.getControllers().stream().anyMatch(c -> c.getJid().equals(this.jid)))
                    {
                        return SelectedState.MULTI_SELECTED;
                    }
                }
            }
            return SelectedState.NONE;
        }

        @Nullable
        private Controller getController()
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                if(controller.getJid().equals(this.jid))
                {
                    return controller;
                }
                if(controller instanceof MultiController m)
                {
                    return m.getControllers().stream().filter(c -> c.getJid().equals(this.jid)).findFirst().orElse(null);
                }
            }
            return null;
        }

        public enum SelectedState
        {
            NONE,
            SELECTED,
            MULTI_SELECTED
        }
    }
}
