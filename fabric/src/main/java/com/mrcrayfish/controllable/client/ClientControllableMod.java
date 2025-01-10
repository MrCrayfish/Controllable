package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

/**
 * Author: MrCrayfish
 */
public class ClientControllableMod implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientBootstrap.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Controllable.getControllerManager().dispose();
        });
    }
}
