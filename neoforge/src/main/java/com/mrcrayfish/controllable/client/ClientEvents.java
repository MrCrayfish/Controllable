package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event)
    {
        Controllable.getControllerManager().completeSetup();
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        //RumbleHandler.onPlayerUsingItem(event.getEntity(), event.getItem(), event.getDuration());
    }
}
