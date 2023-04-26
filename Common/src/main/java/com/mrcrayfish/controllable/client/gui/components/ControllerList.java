package com.mrcrayfish.controllable.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.ControllerManager;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ControllerList extends TabSelectionList<ControllerList.ControllerEntry>
{
    private final ControllerManager manager;
    private final MutableComponent footerSubText;
    private int controllerCount;

    public ControllerList(Minecraft mc, int itemHeight)
    {
        super(mc, itemHeight);
        this.manager = Controllable.getManager();
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
        return Objects.equals(this.getSelected(), this.children().get(index));
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
            if(ScreenUtil.isMouseWithin(this.x0 + (this.width + footerWidth) / 2 - footerSubWidth, this.y1 + 4, footerSubWidth, 14, (int) mouseX, (int) mouseY))
            {
                Objects.requireNonNull(Minecraft.getInstance().screen).handleComponentClicked(this.footerSubText.getStyle());
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

        public int getDeviceIndex()
        {
            return this.deviceIndex;
        }

        @Override
        public void render(PoseStack poseStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            // Draws a transparent black background on every odd item to help match the widgets with the label
            if(ControllerList.this.getSelected() == this)
            {
                RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/container/beacon.png"));
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                Screen.blit(poseStack, left + 3, top + 3, 91, 224, 14, 12, 256, 256);
            }
            else if(slotIndex % 2 != 0)
            {
                Screen.fill(poseStack, left - 2, top - 2, left + listWidth + 2, top + slotHeight + 2, 0x55000000);
            }
            Font font = Minecraft.getInstance().font;
            GuiComponent.drawString(poseStack, font, this.label, left + 22, top + (slotHeight - font.lineHeight) / 2 + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                if(ControllerList.this.getSelected() != this)
                {
                    ControllerList.this.setSelected(this);
                    manager.setActiveController(manager.createController(this.deviceIndex, this.jid));
                }
                else
                {
                    ControllerList.this.setSelected(null);
                    manager.setActiveController(null);
                }
            }
            return false;
        }
    }
}
