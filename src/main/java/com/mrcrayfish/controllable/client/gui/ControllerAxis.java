package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

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
    public void draw(PoseStack poseStack, int x, int y, int mouseX, int mouseY, boolean selected)
    {
        poseStack.pushPose();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            GLFWGamepadState gamepad = controller.getGamepadState();
            switch(this.button)
            {
                case Buttons.LEFT_THUMB_STICK:
                    float leftX = gamepad.axes(this.screen.getEntry().isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X : GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
                    float leftY = gamepad.axes(this.screen.getEntry().isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y : GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
                    leftX *= this.screen.getEntry().isFlipLeftX() ? -1 : 1;
                    leftY *= this.screen.getEntry().isFlipLeftY() ? -1 : 1;
                    poseStack.translate(leftX * 5, leftY * 5, 0);
                    break;
                case Buttons.RIGHT_THUMB_STICK:
                    float rightX = gamepad.axes(this.screen.getEntry().isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_LEFT_X : GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
                    float rightY = gamepad.axes(this.screen.getEntry().isThumbsticksSwitched() ? GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y : GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
                    rightX *= this.screen.getEntry().isFlipRightX() ? -1 : 1;
                    rightY *= this.screen.getEntry().isFlipRightY() ? -1 : 1;
                    poseStack.translate(rightX * 5, rightY * 5, 0);
                    break;
            }

            if(!this.screen.isButtonPressed(this.button))
            {
                poseStack.translate(0, -5, 0);
            }
        }
        super.draw(poseStack, x, y, mouseX, mouseY, selected);
        poseStack.popPose();
    }
}
