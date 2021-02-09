package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.IToolTip;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends Screen
{
    private static final AbstractOption[] OPTIONS = new AbstractOption[]{ControllerOptions.AUTO_SELECT, ControllerOptions.RENDER_MINI_PLAYER, ControllerOptions.VIRTUAL_MOUSE, ControllerOptions.CONSOLE_HOTBAR, ControllerOptions.CONTROLLER_ICONS, ControllerOptions.CURSOR_TYPE, ControllerOptions.INVERT_LOOK, ControllerOptions.DEAD_ZONE, ControllerOptions.ROTATION_SPEED, ControllerOptions.MOUSE_SPEED, ControllerOptions.SHOW_ACTIONS, ControllerOptions.QUICK_CRAFT};
    private final Screen parentScreen;
    private IToolTip hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.settings"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            AbstractOption option = OPTIONS[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1);
            this.addButton(option.createWidget(this.minecraft.gameSettings, x, y, 150));
        }

        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 24 * (OPTIONS.length + 1) / 2, 200, 20, DialogTexts.GUI_BACK, (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
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
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        this.hoveredTooltip = this.getHoveredToolTip(mouseX, mouseY);
        if(this.hoveredTooltip != null && this.hoveredCounter >= 20)
        {
            this.renderTooltip(matrixStack, this.hoveredTooltip.getToolTip(), mouseX, mouseY);
        }
    }

    @Nullable
    private IToolTip getHoveredToolTip(int mouseX, int mouseY)
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
}
