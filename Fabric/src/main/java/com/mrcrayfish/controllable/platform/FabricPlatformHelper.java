package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.platform.services.IPlatformHelper;

/**
 * Author: MrCrayfish
 */
public class FabricPlatformHelper implements IPlatformHelper
{
    @Override
    public boolean isFabric()
    {
        return true;
    }
}
