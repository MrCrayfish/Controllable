package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import com.mrcrayfish.framework.FrameworkSetup;
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
        FrameworkSetup.run();
        bus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(ClientBootstrap::init);
    }
}
