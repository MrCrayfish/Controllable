package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class ThumbstickSettingsScreen extends Screen
{
    private OptionsList optionsRowList;
    private final ControllerLayoutScreen layoutScreen;

    protected ThumbstickSettingsScreen(ControllerLayoutScreen layoutScreen)
    {
        super(Component.translatable("controllable.gui.title.thumbstick_settings"));
        this.layoutScreen = layoutScreen;
    }

    @Override
    protected void init()
    {
        this.optionsRowList = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

        /*this.optionsRowList.addBig(ControllerOptions.createOnOff("controllable.options.switchThumbsticks", gameSettings -> {
            return this.layoutScreen.getEntry().isThumbsticksSwitched();
        }, (options, option, value) -> {
            this.layoutScreen.getEntry().setSwitchThumbsticks(value);
        }));

        this.optionsRowList.addSmall(ControllerOptions.createOnOff("controllable.options.flipLeftThumbstickX", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipLeftX();
        }, (options, option, value) -> {
            this.layoutScreen.getEntry().setFlipLeftX(value);
        }), ControllerOptions.createOnOff("controllable.options.flipRightThumbstickX", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipRightX();
        }, (options, option, value) -> {
            this.layoutScreen.getEntry().setFlipRightX(value);
        }));

        this.optionsRowList.addSmall(ControllerOptions.createOnOff("controllable.options.flipLeftThumbstickY", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipLeftY();
        }, (options, option, value) -> {
            this.layoutScreen.getEntry().setFlipLeftY(value);
        }), ControllerOptions.createOnOff("controllable.options.flipRightThumbstickY", gameSettings -> {
            return this.layoutScreen.getEntry().isFlipRightY();
        }, (options, option, value) -> {
            this.layoutScreen.getEntry().setFlipRightY(value);
        }));*/

        this.addWidget(this.optionsRowList);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.layoutScreen);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        this.optionsRowList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
