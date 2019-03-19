package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GuiControllerLayout extends GuiScreen
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    private List<ControllerButton> controllerButtons = new ArrayList<>();

    @Override
    public void initGui()
    {
        controllerButtons.add(new ControllerButton(Buttons.A, 29, 11, 7, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.B, 32, 8, 13, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.X, 26, 8, 16, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.Y, 29, 5, 10, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.LEFT_BUMPER, 5, 0, 25, 0, 7, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.RIGHT_BUMPER, 26, 0, 32, 0, 7, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.LEFT_TRIGGER, 5, -8, 39, 0, 7, 6, 5));
        controllerButtons.add(new ControllerButton(Buttons.RIGHT_TRIGGER, 26, -8, 39, 0, 7, 6, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_DOWN, 6, 11, 19, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_RIGHT, 9, 8, 19, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_LEFT, 3, 8, 19, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_UP, 6, 5, 19, 0, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.SELECT, 14, 6, 22, 0, 3, 2, 5));
        controllerButtons.add(new ControllerButton(Buttons.START, 21, 6, 22, 0, 3, 2, 5));
        controllerButtons.add(new ControllerButton(Buttons.HOME, 17, 10, 46, 0, 4, 4, 5));
        controllerButtons.add(new ControllerAxis(Buttons.LEFT_THUMB_STICK, 9, 14, 0, 0, 7, 7, 5));
        controllerButtons.add(new ControllerAxis(Buttons.RIGHT_THUMB_STICK, 22, 14, 0, 0, 7, 7, 5));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TEXTURE);
        int width = 38 * 5;
        int height = 29 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50;
        drawScaledCustomSizeModalRect(x, y, 0, 14, 38, 29, width, height, 256, 256);
        GlStateManager.disableBlend();
        controllerButtons.forEach(controllerButton -> controllerButton.draw(x, y, mouseX, mouseY));
    }
}
