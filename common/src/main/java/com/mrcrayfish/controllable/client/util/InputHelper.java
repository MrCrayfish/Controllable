package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Controller;

/**
 * Author: MrCrayfish
 */
public class InputHelper
{
    public static float getCombinedPressedValue(Controller controller, ButtonBinding first, ButtonBinding second)
    {
        float firstValue = controller.getPressedValue(first.getButton());
        float secondValue = controller.getPressedValue(second.getButton());
        if(firstValue > 0 && secondValue > 0) // Both pressed equals centered
            return 0;
        return secondValue - firstValue;
    }
}
