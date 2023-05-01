package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.IToolTip;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends ListMenuScreen
{
    private static final AbstractOption[] OPTIONS = new AbstractOption[]{
            ControllerOptions.AUTO_SELECT,
            ControllerOptions.RENDER_MINI_PLAYER,
            ControllerOptions.VIRTUAL_MOUSE,
            ControllerOptions.CONSOLE_HOTBAR,
            ControllerOptions.CONTROLLER_ICONS,
            ControllerOptions.CURSOR_TYPE,
            ControllerOptions.INVERT_LOOK,
            ControllerOptions.DEAD_ZONE,
            ControllerOptions.ROTATION_SPEED,
            ControllerOptions.MOUSE_SPEED,
            ControllerOptions.SHOW_ACTIONS,
            ControllerOptions.QUICK_CRAFT,
            ControllerOptions.UI_SOUNDS,
            ControllerOptions.RADIAL_THUMBSTICK,
            ControllerOptions.SNEAK_MODE,
            ControllerOptions.CURSOR_THUMBSTICK,
            ControllerOptions.HOVER_MODIFIER
    };
    private IToolTip hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(Screen parent)
    {
        super(parent, new TranslationTextComponent("controllable.gui.title.settings"), 24);
        this.setSearchBarVisible(false);
        this.setRowWidth(310);
    }

    @Override
    protected void init()
    {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, this.height - 32, 200, 20, DialogTexts.GUI_BACK, (button) -> {
            this.minecraft.displayGuiScreen(this.parent);
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
    private AbstractOption getOption(int index)
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.hoveredTooltip = this.getHoveredToolTip(mouseX, mouseY);
        if(this.hoveredTooltip != null && this.hoveredCounter >= 20)
        {
            this.renderTooltip(matrixStack, this.hoveredTooltip.getToolTip(), mouseX, mouseY);
        }
    }

    @Nullable
    private IToolTip getHoveredToolTip(int mouseX, int mouseY) //TODO fix
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            AbstractOption option = OPTIONS[i];
            if(!(option instanceof IToolTip))
                continue;
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1);
            if(mouseX >= x && mouseY >= y && mouseX < x + 150 && mouseY < y + 20)
            {
                return (IToolTip) option;
            }
        }
        return null;
    }

    public class OptionRowItem extends Item
    {
        private final Widget optionOne;
        private final Widget optionTwo;

        public OptionRowItem(@Nullable AbstractOption o1, @Nullable AbstractOption o2)
        {
            super(StringTextComponent.EMPTY);
            this.optionOne = o1.createWidget(SettingsScreen.this.minecraft.gameSettings, 0, 0, 150);
            this.optionTwo = Optional.ofNullable(o2).map(o -> o.createWidget(SettingsScreen.this.minecraft.gameSettings, 0, 0, 150)).orElse(null);

        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks)
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
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return this.optionTwo != null ? ImmutableList.of(this.optionOne, this.optionTwo) : ImmutableList.of(this.optionOne);
        }
    }
}
