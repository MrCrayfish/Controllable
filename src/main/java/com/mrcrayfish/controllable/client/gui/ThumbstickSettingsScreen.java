package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.settings.ControllableBooleanOption;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Author: MrCrayfish
 */
public class ThumbstickSettingsScreen extends Screen
{
    private OptionsRowList optionsRowList;
    private final ControllerLayoutScreen layoutScreen;

    protected ThumbstickSettingsScreen(ControllerLayoutScreen layoutScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.thumbstick_settings"));
        this.layoutScreen = layoutScreen;
    }

    @Override
    protected void init()
    {
        this.optionsRowList = new OptionsRowList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

        this.optionsRowList.addOption(new ControllableBooleanOption("controllable.options.switchThumbsticks", gameSettings -> {
            return this.layoutScreen.getEntry().isThumbsticksSwitched();
        }, (gameSettings, value) -> {
            this.layoutScreen.getEntry().setSwitchThumbsticks(value);
        }));

        this.optionsRowList.addOption(new ControllableBooleanOption("controllable.options.flipLeftThumbstickX", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipLeftX();
        }, (gameSettings, value) -> {
            this.layoutScreen.getEntry().setFlipLeftX(value);
        }), new ControllableBooleanOption("controllable.options.flipRightThumbstickX", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipRightX();
        }, (gameSettings, value) -> {
            this.layoutScreen.getEntry().setFlipRightX(value);
        }));

        this.optionsRowList.addOption(new ControllableBooleanOption("controllable.options.flipLeftThumbstickY", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipLeftY();
        }, (gameSettings, value) -> {
            this.layoutScreen.getEntry().setFlipLeftY(value);
        }), new ControllableBooleanOption("controllable.options.flipRightThumbstickY", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipRightY();
        }, (gameSettings, value) -> {
            this.layoutScreen.getEntry().setFlipRightY(value);
        }));

        this.children.add(this.optionsRowList);

        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_BACK, (button) -> {
            this.minecraft.displayGuiScreen(this.layoutScreen);
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
