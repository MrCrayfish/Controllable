package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.KeyMapping;

/**
 * Fixes a compatibility issue with Spyglass Improvements. Hopefully this hack doesn't cause any
 * issues with other mods.
 *
 * Author: MrCrayfish
 */
public class KeyUseOverride extends KeyMapping
{
    public KeyUseOverride(KeyMapping mapping)
    {
        super(mapping.getName(), mapping.getDefaultKey().getType(), mapping.getDefaultKey().getValue(), mapping.getCategory());
    }

    @Override
    public boolean isDown()
    {
        return super.isDown() || isRightClicking();
    }

    /**
     * Checks if a controller is connected and if the use item button is down.
     */
    public static boolean isRightClicking()
    {
        Controller controller = Controllable.getController();
        return controller != null && ButtonBindings.USE_ITEM.isButtonDown();
    }
}
