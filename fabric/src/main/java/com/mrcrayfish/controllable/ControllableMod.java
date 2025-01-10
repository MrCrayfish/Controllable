package com.mrcrayfish.controllable;

import com.mrcrayfish.framework.FrameworkSetup;
import net.fabricmc.api.ModInitializer;

public class ControllableMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        // On Fabric, no guarantee Framework initialization happens before Controllable
        // So we need to run the setup ourselves.
        FrameworkSetup.run();
    }
}
