package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.option.Option;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * Author: MrCrayfish
 */
public class GuiControllerSettings extends GuiScreen
{
    private static final Option[] OPTIONS = new Option[]{ControllerOptions.AUTO_SELECT, ControllerOptions.DEAD_ZONE, ControllerOptions.ROTATION_SPEED, ControllerOptions.MOUSE_SPEED};

    private GuiScreen previous;

    public GuiControllerSettings(GuiScreen previous)
    {
        this.previous = previous;
    }

    @Override
    public void initGui()
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            Option option = OPTIONS[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1);
            this.addButton(option.createOption(i, x, y, 150));
        }

        this.addButton(new GuiButton(OPTIONS.length, this.width / 2 - 100, this.height / 6 + 24 * (OPTIONS.length + 1) / 2, 200, 20, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if(button.id == OPTIONS.length)
        {
            this.mc.displayGuiScreen(this.previous);
        }
    }

    @Override
    public void onGuiClosed()
    {
        Controllable.getOptions().saveOptions();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(mc.fontRenderer, I18n.format("controllable.gui.title.settings"), this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
