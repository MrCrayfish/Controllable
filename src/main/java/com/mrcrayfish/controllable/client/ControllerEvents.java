package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumAction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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

            EnumAction action = event.getItem().getItemUseAction();
            switch(action)
            {
                case BLOCK:
                    magnitudeFactor = 0.25F;
                    break;
                case BOW:
                    magnitudeFactor = MathHelper.clamp((event.getItem().getMaxItemUseDuration() - event.getDuration()) / 20F, 0.0F, 1.0F);
                    break;
            }
            controller.getSDL2Controller().rumble(0.5F * magnitudeFactor, 0.5F * magnitudeFactor, 50);
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

        Minecraft mc = Minecraft.getMinecraft();
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
