package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AbstractOption;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Based on {@link SettingsScreen} by Author: MrCrayfish
 *
 * This was made by Fernthedev
 */
public class AimAssistSettingsScreen extends Screen
{
    private static final AbstractOption[] OPTIONS = new AbstractOption[]{ControllerOptions.TOGGLE_AIM, ControllerOptions.AIM_ASSIST_INTENSITY, ControllerOptions.ANIMAL_AIM_MODE, ControllerOptions.HOSTILE_AIM_MODE, ControllerOptions.PLAYER_AIM_MODE};
    private final Screen parentScreen;

    protected AimAssistSettingsScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.settings.aim_assist"));
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

        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 24 * (OPTIONS.length + 1) / 2, 200, 20, I18n.format("gui.done"), (button) -> this.minecraft.displayGuiScreen(this.parentScreen)));
    }

    @Override
    public void removed()
    {
        Controllable.getOptions().saveOptions();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 20, 0xFFFFFF);

        if (Controllable.getOptions().isAimAssist()) {
            drawCenteredString(minecraft.fontRenderer, TextFormatting.YELLOW + I18n.format("controllable.gui.title.settings.aim_assist.warning1"), this.width / 2, this.height / 6 + 32 * (OPTIONS.length + 1) / 2, 0xFFFFFF);
            drawCenteredString(minecraft.fontRenderer, TextFormatting.RED + I18n.format("controllable.gui.title.settings.aim_assist.warning2"), this.width / 2, this.height / 6 + 36 * (OPTIONS.length + 1) / 2, 0xFFFFFF);
            drawCenteredString(minecraft.fontRenderer, I18n.format("controllable.gui.title.settings.aim_assist.warning3"), this.width / 2, this.height / 6 + 40 * (OPTIONS.length + 1) / 2, 0xFFFFFF);

            if (Controllable.getOptions().getAimAssistIntensity() > 90) {
                drawCenteredString(minecraft.fontRenderer,TextFormatting.RED + I18n.format("controllable.gui.title.settings.aim_assist_intensity.warning"), this.width / 2, this.height / 6 + 44 * (OPTIONS.length + 1) / 2, 0xFFFFFF);

            }
        }

        super.render(mouseX, mouseY, partialTicks);
    }
}
