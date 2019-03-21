package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.renderer.GlStateManager;

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
    public void draw(int x, int y, int mouseX, int mouseY)
    {
        GlStateManager.pushMatrix();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            switch(button)
            {
                case Buttons.LEFT_THUMB_STICK:
                    GlStateManager.translate(controller.getLThumbStickXValue() * 5, -controller.getLThumbStickYValue() * 5, 0);
                    break;
                case Buttons.RIGHT_THUMB_STICK:
                    GlStateManager.translate(controller.getRThumbStickXValue() * 5, -controller.getRThumbStickYValue() * 5, 0);
                    break;
            }

            if(!controller.isButtonPressed(button))
            {
                GlStateManager.translate(0, -2.5, 0);
            }
        }
        super.draw(x, y, mouseX, mouseY);
        GlStateManager.popMatrix();
    }
}
