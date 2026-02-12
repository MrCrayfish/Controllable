package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class ControllableMod
{
    @SubscribeEvent
    private static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(ClientBootstrap::init);
    }

    @SubscribeEvent
    private static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            Controllable.getBindingRegistry().completeSetup();
            Controllable.getControllerManager().completeSetup();
            Controllable.getCursor().resetToCenter();
        });
    }
}