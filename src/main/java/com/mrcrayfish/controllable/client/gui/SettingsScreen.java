package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.IToolTip;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SettingsScreen extends Screen
{
    private static final Option[] OPTIONS = new Option[]{ControllerOptions.AUTO_SELECT, ControllerOptions.RENDER_MINI_PLAYER, ControllerOptions.VIRTUAL_MOUSE, ControllerOptions.CONSOLE_HOTBAR, ControllerOptions.CONTROLLER_ICONS, ControllerOptions.CURSOR_TYPE, ControllerOptions.INVERT_LOOK, ControllerOptions.DEAD_ZONE, ControllerOptions.ROTATION_SPEED, ControllerOptions.MOUSE_SPEED, ControllerOptions.SHOW_ACTIONS, ControllerOptions.QUICK_CRAFT, ControllerOptions.UI_SOUNDS, ControllerOptions.RADIAL_THUMBSTICK, ControllerOptions.SNEAK_MODE};
    private final Screen parentScreen;
    private IToolTip hoveredTooltip;
    private int hoveredCounter;

    protected SettingsScreen(Screen parentScreen)
    {
        super(new TranslatableComponent("controllable.gui.title.settings"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            Option option = OPTIONS[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 24 * (i >> 1) - 12;
            this.addRenderableWidget(option.createButton(this.minecraft.options, x, y, 150));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 24 * (OPTIONS.length + 1) / 2 - 12, 200, 20, CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.parentScreen);
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        this.hoveredTooltip = this.getHoveredToolTip(mouseX, mouseY);
        if(this.hoveredTooltip != null && this.hoveredCounter >= 20)
        {
            this.renderTooltip(poseStack, this.hoveredTooltip.getToolTip(), mouseX, mouseY);
        }
    }

    @Nullable
    private IToolTip getHoveredToolTip(int mouseX, int mouseY)
    {
        for(int i = 0; i < OPTIONS.length; i++)
        {
            Option option = OPTIONS[i];
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
