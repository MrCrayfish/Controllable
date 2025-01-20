package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class InputHelper
{
    public static float getCombinedPressedValue(Controller controller, ButtonBinding first, ButtonBinding second)
    {
        ButtonBinding active = Controllable.getInputHandler().getActiveVirtualBinding();
        float firstValue = active == first ? controller.getPressedValue(ButtonBindings.RADIAL_MENU.getButton()) : controller.getPressedValue(first.getButton());
        float secondValue = active == second ? controller.getPressedValue(ButtonBindings.RADIAL_MENU.getButton()) : controller.getPressedValue(second.getButton());
        if(firstValue > 0 && secondValue > 0) // Both pressed equals centered
            return 0;
        return secondValue - firstValue;
    }

    public static float applyDeadzone(float input, float deadZone)
    {
        return Mth.sign(input) * Math.max(Mth.abs(input) - deadZone, 0.0F) / (1.0F - deadZone);
    }
}
