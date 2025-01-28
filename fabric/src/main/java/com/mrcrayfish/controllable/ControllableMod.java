package com.mrcrayfish.controllable;

import com.mrcrayfish.framework.FrameworkSetup;
import net.fabricmc.api.ModInitializer;

/**
 * Author: MrCrayfish
 */
public class ControllableMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        FrameworkSetup.run();
    }
}
