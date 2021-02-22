package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.IToolTip;
import com.mrcrayfish.controllable.client.gui.widget.PressableButton;
import com.mrcrayfish.controllable.client.settings.ControllableOption;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends GuiScreen
{
    private static final ControllableOption[] OPTIONS = new ControllableOption[]{ControllerOptions.AUTO_SELECT, ControllerOptions.RENDER_MINI_PLAYER, ControllerOptions.VIRTUAL_MOUSE, ControllerOptions.CONSOLE_HOTBAR, ControllerOptions.CONTROLLER_TYPE, ControllerOptions.CURSOR_TYPE, ControllerOptions.INVERT_LOOK, ControllerOptions.DEAD_ZONE, ControllerOptions.ROTATION_SPEED, ControllerOptions.MOUSE_SPEED, ControllerOptions.SHOW_ACTIONS, ControllerOptions.QUICK_CRAFT};
    private final GuiScreen parentScreen;
    private IToolTip hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui()
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            ControllableOption option = OPTIONS[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1);
            this.addButton(option.createOption(0, x, y, 150));
        }

        this.addButton(new PressableButton(this.width / 2 - 100, this.height / 6 + 24 * (OPTIONS.length + 1) / 2, 200, 20, I18n.format("gui.done"), (button) -> {
            this.mc.displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void onGuiClosed()
    {
        Controllable.getOptions().saveOptions();
    }

    @Override
    public void updateScreen()
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("controllable.gui.title.settings"), this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.hoveredTooltip = this.getHoveredToolTip(mouseX, mouseY);
        if(this.hoveredTooltip != null && this.hoveredCounter >= 20)
        {
            this.drawHoveringText(this.hoveredTooltip.getToolTip(), mouseX, mouseY);
        }
    }

    @Nullable
    private IToolTip getHoveredToolTip(int mouseX, int mouseY)
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            ControllableOption option = OPTIONS[i];
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
