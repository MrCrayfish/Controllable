package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import com.mrcrayfish.controllable.client.ControllerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class ControllableMod implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientBootstrap.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ControllerManager.instance().close();
        });
    }
}
