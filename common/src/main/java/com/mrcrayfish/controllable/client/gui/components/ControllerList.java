package com.mrcrayfish.controllable.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.input.MultiController;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
    private static final ResourceLocation CHECKMARK = ResourceLocation.withDefaultNamespace("container/beacon/confirm");

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
        this.footerSubText.setStyle(this.footerSubText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mrcrayfish.gitbook.io/controllable-documentation/")));
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

    @Override
    protected boolean isSelectedItem(int index)
    {
        return false;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.footerText != null)
        {
            Font font = this.minecraft.font;
            int footerWidth = font.width(this.footerText);
            int footerSubWidth = font.width(this.footerSubText);
            if(ScreenHelper.isMouseWithin(this.getX() + (this.width + footerWidth) / 2 - footerSubWidth, this.getBottom() + 4, footerSubWidth, 14, (int) mouseX, (int) mouseY))
            {
                Objects.requireNonNull(Minecraft.getInstance().screen).handleComponentClicked(this.footerSubText.getStyle());
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderListItems(graphics, mouseX, mouseY, partialTick);
        this.renderLinkedItems(graphics, mouseX, mouseY);
    }

    private void renderLinkedItems(GuiGraphics graphics, int mouseX, int mouseY)
    {
        Controller controller = Controllable.getController();
        if(controller instanceof MultiController multi)
        {
            Set<Number> jids = multi.getControllers().stream().map(Controller::getJid).collect(Collectors.toSet());
            Set<Integer> matchedEntries = new HashSet<>();
            int start = -1, end = -1;
            for(int i = 0; i < this.getItemCount(); i++)
            {
                ControllerEntry entry = this.getEntry(i);
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
                int itemCenter = (this.itemHeight - 4) / 2;
                int rowLeft = this.getRowLeft();
                int lineTop = this.getRowTop(start) + itemCenter;
                int lineEnd = this.getRowTop(end) + itemCenter;
                graphics.fill(rowLeft - 12, lineTop, rowLeft - 10, lineEnd, 0xFFFFFFFF);

                int iconTop = lineTop + (lineEnd - lineTop) / 2 - 7;
                int iconLeft = rowLeft - 30;
                graphics.blit(Icons.TEXTURE, iconLeft, iconTop, 14, 14, 110, 0, 11, 11, Icons.TEXTURE_WIDTH, Icons.TEXTURE_HEIGHT);

                for(int i : matchedEntries)
                {
                    int rowTop = this.getRowTop(i);
                    graphics.fill(rowLeft - 11, rowTop + itemCenter - 1, rowLeft - 4, rowTop + itemCenter + 1, 0xFFFFFFFF);
                }

                if(ScreenHelper.isMouseWithin(iconLeft, iconTop, 14, 14, mouseX, mouseY))
                {
                    this.holder.setTooltipForNextRenderPass(this.createLinkTooltip(), DefaultTooltipPositioner.INSTANCE, true);
                }
            }
        }
    }

    private Tooltip createLinkTooltip()
    {
        List<FormattedText> lines = new ArrayList<>();
        lines.add(Component.translatable("controllable.gui.linked_controllers").withStyle(ChatFormatting.AQUA));
        lines.addAll(this.minecraft.font.getSplitter().splitLines(Component.translatable("controllable.gui.linked_controllers.desc"), 200, Style.EMPTY));
        return ClientHelper.createListTooltip(lines);
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
        public void render(GuiGraphics graphics, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            State state = this.getState();
            if(state != State.NONE)
            {
                ScreenHelper.drawRoundedBox(graphics, left - 1, top - 1, listWidth + 2, slotHeight + 2, 0xFFFFFFFF);
                ScreenHelper.drawRoundedBox(graphics, left, top, listWidth, slotHeight, 0xFF000000);
                graphics.blitSprite(CHECKMARK, left + 2, top, 18, 18);
            }
            else if(Controllable.getController() != null && hovered)
            {
                graphics.blit(Icons.TEXTURE, left + 4, top + 4, 11, 11, 110, 0, 11, 11, Icons.TEXTURE_WIDTH, Icons.TEXTURE_HEIGHT);
                holder.setTooltipForNextRenderPass(Component.translatable("controllable.gui.link").withStyle(ChatFormatting.AQUA));
            }
            Font font = Minecraft.getInstance().font;
            graphics.drawString(font, this.label, left + 22, top + (slotHeight - font.lineHeight) / 2 + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_1)
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

        private State getState()
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                if(controller.getJid().equals(this.jid))
                    return State.SELECTED;

                if(controller instanceof MultiController m)
                {
                    if(m.getControllers().stream().anyMatch(c -> c.getJid().equals(this.jid)))
                    {
                        return State.MULTI_SELECTED;
                    }
                }
            }
            return State.NONE;
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

        public enum State
        {
            NONE,
            SELECTED,
            MULTI_SELECTED
        }
    }
}
