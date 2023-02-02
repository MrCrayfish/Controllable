package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import com.mrcrayfish.controllable.client.settings.ControllerSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends ListMenuScreen
{
    private List<FormattedCharSequence> hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(Screen parent)
    {
        super(parent, Component.translatable("controllable.gui.title.settings"), 24);
        this.setSearchBarVisible(false);
        this.setRowWidth(310);
    }

    @Override
    protected void init()
    {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).pos(this.width / 2 - 100, this.height - 32).size(200, 20).build());
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        entries.add(new TitleItem(Component.translatable("controllable.gui.title.general").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        entries.add(new WidgetRow(ControllerOptions.MOUSE_SPEED, ControllerOptions.LIST_SCROLL_SPEED));
        entries.add(new WidgetRow(ControllerOptions.DEAD_ZONE, ControllerOptions.HOVER_MODIFIER));
        entries.add(new WidgetRow(ControllerOptions.RADIAL_THUMBSTICK, ControllerOptions.CURSOR_THUMBSTICK));

        entries.add(new TitleItem(Component.translatable("controllable.gui.title.gameplay").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        entries.add(new WidgetRow(ControllerOptions.ROTATION_SPEED));
        entries.add(new WidgetRow(ControllerOptions.INVERT_LOOK, ControllerOptions.INVERT_ROTATION));
        entries.add(new WidgetRow(ControllerOptions.QUICK_CRAFT, ControllerOptions.SNEAK_MODE));

        entries.add(new TitleItem(Component.translatable("controllable.gui.title.display").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        entries.add(new WidgetRow(ControllerOptions.RENDER_MINI_PLAYER, ControllerOptions.CONSOLE_HOTBAR));
        entries.add(new WidgetRow(ControllerOptions.CONTROLLER_ICONS, ControllerOptions.SHOW_ACTIONS));
        entries.add(new WidgetRow(ControllerOptions.CURSOR_TYPE, ControllerOptions.HINT_BACKGROUND));

        entries.add(new TitleItem(Component.translatable("controllable.gui.title.sounds").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        entries.add(new WidgetRow(ControllerOptions.UI_SOUNDS));

        entries.add(new TitleItem(Component.translatable("controllable.gui.title.advanced").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        entries.add(new WidgetRow(ControllerOptions.VIRTUAL_MOUSE, ControllerOptions.AUTO_SELECT));
        entries.add(new WidgetRow(ControllerOptions.FPS_POLLING_FIX, null));
    }

    @Override
    public void onClose()
    {
        Config.save();
    }

    @Override
    public void tick()
    {
        if(this.hoveredTooltip != null)
        {
            if(this.hoveredCounter < 20)
            {
                this.hoveredCounter++;
            }
        }
        else
        {
            this.hoveredCounter = 0;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(poseStack, mouseX, mouseY, partialTicks);
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

    public class WidgetRow extends Item
    {
        private final AbstractWidget optionOne;
        private final AbstractWidget optionTwo;

        public WidgetRow(ControllerSetting<?> leftWidget)
        {
            super(CommonComponents.EMPTY);
            this.optionOne = leftWidget.createWidget(0, 0, 310, 20).get();
            this.optionTwo = null;
        }

        public WidgetRow(ControllerSetting<?> leftWidget, @Nullable ControllerSetting<?> rightWidget)
        {
            super(CommonComponents.EMPTY);
            this.optionOne = leftWidget.createWidget(0, 0, 150, 20).get();
            this.optionTwo = Optional.ofNullable(rightWidget).map(o -> o.createWidget(0, 0, 150, 20).get()).orElse(null);
        }

        @Override
        public void render(PoseStack matrixStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            this.optionOne.setX(left);
            this.optionOne.setY(top);
            this.optionOne.render(matrixStack, mouseX, mouseY, partialTicks);
            if(this.optionTwo != null)
            {
                this.optionTwo.setX(left + width - 150);
                this.optionTwo.setY(top);
                this.optionTwo.render(matrixStack, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return this.optionTwo != null ? ImmutableList.of(this.optionOne, this.optionTwo) : ImmutableList.of(this.optionOne);
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
}
