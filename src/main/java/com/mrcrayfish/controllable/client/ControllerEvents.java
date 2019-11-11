package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    private float prevHealth = -1;

    @SubscribeEvent(receiveCanceled = true)
    public void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        if(!Controllable.getOptions().useForceFeedback())
        {
            return;
        }

        /* Stops vibration from running because controller is not in use */
        if(Controllable.getInput().getLastUse() <= 0)
        {
            return;
        }

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            UseAction action = event.getItem().getUseAction();
            switch(action)
            {
                case BLOCK:
                    magnitudeFactor = 0.25F;
                    break;
                case SPEAR:
                    magnitudeFactor = MathHelper.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 0.25F) / 0.25F;
                    break;
                case BOW:
                    magnitudeFactor = MathHelper.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.0F) / 1.0F;
                    break;
                case CROSSBOW:
                    magnitudeFactor = MathHelper.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.5F) / 1.5F;
                    break;
            }
            controller.getSDL2Controller().rumble(0.5F * magnitudeFactor, 0.5F * magnitudeFactor, 50); //50ms is one tick
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
        {
            return;
        }

        Controller controller = Controllable.getController();
        if(controller == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.world != null && Controllable.getOptions().useForceFeedback())
        {
            if(prevHealth == -1)
            {
                prevHealth = mc.player.getHealth();
            }
            else if(prevHealth > mc.player.getHealth())
            {
                float difference = prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                controller.getSDL2Controller().rumble(1.0F, 1.0F, (int) (800 * magnitude));
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
