package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.renderer.GlStateManager;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;

import static org.libsdl.SDL.*;

/**
 * Author: MrCrayfish
 */
public class ControllerAxis extends ControllerButton
{
    public ControllerAxis(ControllerLayoutScreen screen, int button, int x, int y, int u, int v, int width, int height, int scale)
    {
        super(screen, button, x, y, u, v, width, height, scale);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean selected)
    {
        GlStateManager.pushMatrix();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            SDL2Controller sdl2Controller = controller.getSDL2Controller();
            switch(this.button)
            {
                case Buttons.LEFT_THUMB_STICK:
                    float leftX = sdl2Controller.getAxis(this.screen.getEntry().isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_RIGHTX : SDL_CONTROLLER_AXIS_LEFTX);
                    float leftY = sdl2Controller.getAxis(this.screen.getEntry().isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_RIGHTY : SDL_CONTROLLER_AXIS_LEFTY);
                    leftX *= this.screen.getEntry().isFlipLeftX() ? -1 : 1;
                    leftY *= this.screen.getEntry().isFlipLeftY() ? -1 : 1;
                    GlStateManager.translate(leftX * 5, leftY * 5, 0);
                    break;
                case Buttons.RIGHT_THUMB_STICK:
                    float rightX = sdl2Controller.getAxis(this.screen.getEntry().isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_LEFTX : SDL_CONTROLLER_AXIS_RIGHTX);
                    float rightY = sdl2Controller.getAxis(this.screen.getEntry().isThumbsticksSwitched() ? SDL_CONTROLLER_AXIS_LEFTY : SDL_CONTROLLER_AXIS_RIGHTY);
                    rightX *= this.screen.getEntry().isFlipRightX() ? -1 : 1;
                    rightY *= this.screen.getEntry().isFlipRightY() ? -1 : 1;
                    GlStateManager.translate(rightX * 5, rightY * 5, 0);
                    break;
            }

            if(!this.screen.isButtonPressed(button))
            {
                GlStateManager.translate(0, -2.5, 0);
            }
        }
        super.draw(x, y, mouseX, mouseY, selected);
        GlStateManager.popMatrix();
    }
}
