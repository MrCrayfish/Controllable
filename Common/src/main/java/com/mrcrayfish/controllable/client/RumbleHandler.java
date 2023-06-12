package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Author: MrCrayfish
 */
public class RumbleHandler
{
    private static float prevHealth = -1;

    public static void init()
    {
        TickEvents.START_CLIENT.register(RumbleHandler::onClientTick);
    }

    public static void onPlayerUsingItem(Entity entity, ItemStack stack, int duration)
    {
        if(entity != Minecraft.getInstance().player)
            return;

        if(!Config.CLIENT.client.options.rumble.get())
            return;

        if(Controllable.getInput().getLastUse() <= 0)
            return;

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            UseAnim action = stack.getUseAnimation();
            switch(action)
            {
                case BLOCK -> magnitudeFactor = 0.25F;
                case SPEAR -> magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 0.25F) / 0.25F;
                case BOW ->  magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 1.0F);
                case CROSSBOW -> magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 1.5F) / 1.5F;
                case EAT -> magnitudeFactor = 0.15F;
            }
            controller.rumble(0.15F * magnitudeFactor, 0.5F * magnitudeFactor, 70); //50ms is one tick
        }
    }

    private static void onClientTick()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && Config.CLIENT.client.options.rumble.get())
        {
            if(prevHealth == -1 || mc.player.getHealth() > prevHealth)
            {
                prevHealth = mc.player.getHealth();
            }
            else if(prevHealth > mc.player.getHealth())
            {
                float difference = prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                controller.rumble(1.0F, 1.0F, (int) (800 * magnitude));
                prevHealth = mc.player.getHealth();
            }
        }
        else if(prevHealth != -1)
        {
            prevHealth = -1;
        }
    }
}
