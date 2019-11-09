package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MiscEvents {
    @SubscribeEvent(receiveCanceled = true)
    public void onDamageEvent(LivingDamageEvent event)
    {
        if (!event.getEntity().world.isRemote())
            return;
        if (event.getEntityLiving().equals(Minecraft.getInstance().player)) {
            Controller controller = Controllable.getController();
            if (controller == null)
                return;
            float magnatude = event.getAmount() / event.getEntityLiving().getMaxHealth();
            controller.getSDL2Controller().rumble(magnatude, magnatude, (int) (1000 * magnatude));
        }
    }
}
