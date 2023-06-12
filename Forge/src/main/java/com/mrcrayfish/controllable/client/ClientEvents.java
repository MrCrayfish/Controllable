package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event)
    {
        Controllable.getManager().onClientFinishedLoading();
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        RumbleHandler.onPlayerUsingItem(event.getEntity(), event.getItem(), event.getDuration());
    }
}
