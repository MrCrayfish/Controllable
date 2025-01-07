package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Author: MrCrayfish
 */
@Mod(Constants.MOD_ID)
public class ControllableMod
{
    public ControllableMod(IEventBus bus)
    {
        bus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(ClientBootstrap::init);
    }
}
