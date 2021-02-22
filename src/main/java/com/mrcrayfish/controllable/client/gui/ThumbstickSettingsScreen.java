package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.gui.widget.PressableButton;
import com.mrcrayfish.controllable.client.settings.ControllableOption;
import com.mrcrayfish.controllable.client.settings.ControllableOptionBoolean;
import net.minecraft.client.gui.GuiOptionsRowList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * Author: MrCrayfish
 */
public class ThumbstickSettingsScreen extends GuiScreen
{
    private final ControllerLayoutScreen layoutScreen;

    protected ThumbstickSettingsScreen(ControllerLayoutScreen layoutScreen)
    {
        this.layoutScreen = layoutScreen;
    }

    @Override
    public void initGui()
    {
        ControllableOption[] options = new ControllableOption[] {
            new ControllableOptionBoolean("controllable.options.switchThumbsticks", () -> {
                return this.layoutScreen.getEntry().isThumbsticksSwitched();
            }, value -> {
                this.layoutScreen.getEntry().setSwitchThumbsticks(value);
            }, value -> {
                return I18n.format("controllable.options.switchThumbsticks") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
            }),
            new ControllableOptionBoolean("controllable.options.flipLeftThumbstickX", () -> {
                return this.layoutScreen.getEntry().isFlipLeftX();
            }, value -> {
                this.layoutScreen.getEntry().setFlipLeftX(value);
            }, value -> {
                return I18n.format("controllable.options.flipLeftThumbstickX") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
            }),
            new ControllableOptionBoolean("controllable.options.flipRightThumbstickX", () -> {
                return this.layoutScreen.getEntry().isFlipRightX();
            }, value -> {
                this.layoutScreen.getEntry().setFlipRightX(value);
            }, value -> {
                return I18n.format("controllable.options.flipRightThumbstickX") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
            }),
            new ControllableOptionBoolean("controllable.options.flipLeftThumbstickY", () -> {
                return this.layoutScreen.getEntry().isFlipLeftY();
            }, value -> {
                this.layoutScreen.getEntry().setFlipLeftY(value);
            }, value -> {
                return I18n.format("controllable.options.flipLeftThumbstickY") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
            }),
            new ControllableOptionBoolean("controllable.options.flipRightThumbstickY", () -> {
                return this.layoutScreen.getEntry().isFlipRightY();
            }, value -> {
                this.layoutScreen.getEntry().setFlipRightY(value);
            }, value -> {
                return I18n.format("controllable.options.flipRightThumbstickY") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
            })
        };

        for(int i = 0; i < options.length; i++)
        {
            ControllableOption controllableOption = options[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1);
            this.addButton(controllableOption.createOption(i, x, y, 150));
        }

        this.addButton(new PressableButton(this.width / 2 - 100, this.height - 27, 200, 20, I18n.format("gui.back"), (button) -> {
            this.mc.displayGuiScreen(this.layoutScreen);
        }));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("controllable.gui.title.thumbstick_settings"), this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
