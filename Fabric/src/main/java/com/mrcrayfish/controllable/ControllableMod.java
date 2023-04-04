package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import com.mrcrayfish.controllable.client.ClientEvents;
import net.fabricmc.api.ClientModInitializer;

public class ControllableMod implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientBootstrap.init();
        ClientEvents.init();
    }
}
