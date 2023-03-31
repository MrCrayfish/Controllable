package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    private static float prevHealth = -1;

    public static void init()
    {
        TickEvents.START_CLIENT.register(ControllerEvents::onClientTick);
    }

    /*@SubscribeEvent(receiveCanceled = true)
    public void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        if(event.getEntity() != Minecraft.getInstance().player)
        {
            return;
        }

        if(!Config.CLIENT.client.options.forceFeedback.get())
        {
            return;
        }

        Stops vibration from running because controller is not in use
        if(Controllable.getInput().getLastUse() <= 0)
        {
            return;
        }

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            UseAnim action = event.getItem().getUseAnimation();
            switch(action)
            {
                case BLOCK -> magnitudeFactor = 0.25F;
                case SPEAR -> magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 0.25F) / 0.25F;
                case BOW ->  magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.0F);
                case CROSSBOW -> magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.5F) / 1.5F;
            }
            //controller.getGamepadState().rumble(0.5F * magnitudeFactor, 0.5F * magnitudeFactor, 50); //50ms is one tick
        }
    }*/

    private static void onClientTick()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && Config.CLIENT.client.options.forceFeedback.get())
        {
            if(prevHealth == -1)
            {
                prevHealth = mc.player.getHealth();
            }
            else if(prevHealth > mc.player.getHealth())
            {
                float difference = prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                //controller.getGamepadState().rumble(1.0F, 1.0F, (int) (800 * magnitude));
                prevHealth = mc.player.getHealth();
            }
            else
            {
                prevHealth = mc.player.getHealth();
            }
        }
        else if(prevHealth != -1)
        {
            prevHealth = -1;
        }
    }
}
