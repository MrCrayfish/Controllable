package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;

/**
 * Author: MrCrayfish
 */
public class ControllerAxis extends ControllerButton
{
    public ControllerAxis(int button, int x, int y, int u, int v, int width, int height, int scale)
    {
        super(button, x, y, u, v, width, height, scale);
    }

    @Override
    public void draw(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, boolean selected)
    {
        RenderSystem.pushMatrix();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            switch(this.button)
            {
                case Buttons.LEFT_THUMB_STICK:
                    RenderSystem.translatef(controller.getLThumbStickXValue() * 5, -controller.getLThumbStickYValue() * 5, 0);
                    break;
                case Buttons.RIGHT_THUMB_STICK:
                    RenderSystem.translatef(controller.getRThumbStickXValue() * 5, -controller.getRThumbStickYValue() * 5, 0);
                    break;
            }

            if(!Controllable.isButtonPressed(this.button))
            {
                RenderSystem.translated(0, -2.5, 0);
            }
        }
        super.draw(matrixStack, x, y, mouseX, mouseY, selected);
        RenderSystem.popMatrix();
    }
}
