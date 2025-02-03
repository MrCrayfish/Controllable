package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

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
}
