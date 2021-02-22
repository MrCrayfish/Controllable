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
        if(event.getEntity() != Minecraft.getMinecraft().player)
        {
            return;
        }

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
                    //TODO test
                    magnitudeFactor = MathHelper.clamp((event.getItem().getMaxItemUseDuration() - event.getDuration()) / 20F, 0.0F, 1.0F) / 1.0F;
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

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.world != null && Controllable.getOptions().useForceFeedback())
        {
            if(this.prevHealth == -1)
            {
                this.prevHealth = mc.player.getHealth();
            }
            else if(this.prevHealth > mc.player.getHealth())
            {
                float difference = this.prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                controller.getSDL2Controller().rumble(1.0F, 1.0F, (int) (800 * magnitude));
                this.prevHealth = mc.player.getHealth();
            }
            else
            {
                this.prevHealth = mc.player.getHealth();
            }
        }
        else if(this.prevHealth != -1)
        {
            this.prevHealth = -1;
        }
    }
}
