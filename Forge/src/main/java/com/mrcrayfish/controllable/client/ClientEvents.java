package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.GameShuttingDownEvent;
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
        ControllerManager.instance().close();
    }
}
