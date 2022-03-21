package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends ListMenuScreen
{
    private static final Option[] OPTIONS = new Option[]{
            ControllerOptions.AUTO_SELECT, ControllerOptions.RENDER_MINI_PLAYER,
            ControllerOptions.VIRTUAL_MOUSE, ControllerOptions.CONSOLE_HOTBAR,
            ControllerOptions.CONTROLLER_ICONS, ControllerOptions.CURSOR_TYPE,
            ControllerOptions.INVERT_LOOK, ControllerOptions.DEAD_ZONE,
            ControllerOptions.ROTATION_SPEED, ControllerOptions.MOUSE_SPEED,
            ControllerOptions.SHOW_ACTIONS, ControllerOptions.QUICK_CRAFT,
            ControllerOptions.UI_SOUNDS, ControllerOptions.RADIAL_THUMBSTICK,
            ControllerOptions.SNEAK_MODE, ControllerOptions.CURSOR_THUMBSTICK,
            ControllerOptions.HOVER_MODIFIER
    };
    private List<FormattedCharSequence> hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(Screen parent)
    {
        super(parent, new TranslatableComponent("controllable.gui.title.settings"), 24);
        this.setSearchBarVisible(false);
        this.setRowWidth(310);
    }

    @Override
    protected void init()
    {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 32, 200, 20, CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        for(int i = 0; i < OPTIONS.length; i += 2)
        {
            entries.add(new OptionRowItem(this.getOption(i), this.getOption(i + 1)));
        }
    }

    @Nullable
    private Option getOption(int index)
    {
        if(index >= 0 && index < OPTIONS.length)
        {
            return OPTIONS[index];
        }
        return null;
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
        this.hoveredTooltip = this.getHoveredToolTip(mouseX, mouseY);
        if(this.hoveredTooltip != null && this.hoveredCounter >= 10)
        {
            this.renderTooltip(poseStack, this.hoveredTooltip, mouseX, mouseY);
        }
    }

    @Nullable
    private List<FormattedCharSequence> getHoveredToolTip(int mouseX, int mouseY)
    {
        if(this.list.getHovered() instanceof OptionRowItem item)
        {
            List<? extends GuiEventListener> listeners = item.children();
            for(GuiEventListener listener : listeners)
            {
                if(listener.isMouseOver(mouseX, mouseY) && listener instanceof TooltipAccessor accessor)
                {
                    return accessor.getTooltip();
                }
            }
        }
        return null;
    }

    public class OptionRowItem extends Item
    {
        private final AbstractWidget optionOne;
        private final AbstractWidget optionTwo;

        public OptionRowItem(Option o1, @Nullable Option o2)
        {
            super(TextComponent.EMPTY);
            this.optionOne = o1.createButton(SettingsScreen.this.minecraft.options, 0, 0, 150);
            this.optionTwo = Optional.ofNullable(o2).map(o -> o.createButton(SettingsScreen.this.minecraft.options, 0, 0, 150)).orElse(null);

        }

        @Override
        public void render(PoseStack matrixStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks)
        {
            this.optionOne.x = left;
            this.optionOne.y = top;
            this.optionOne.render(matrixStack, mouseX, mouseY, partialTicks);
            if(this.optionTwo != null)
            {
                this.optionTwo.x = left + width - 150;
                this.optionTwo.y = top;
                this.optionTwo.render(matrixStack, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return this.optionTwo != null ? ImmutableList.of(this.optionOne, this.optionTwo) : ImmutableList.of(this.optionOne);
        }
    }
}
