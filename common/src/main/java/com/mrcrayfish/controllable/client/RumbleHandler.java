package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Author: MrCrayfish
 */
public class RumbleHandler
{
    private static RumbleHandler instance;

    @ApiStatus.Internal
    public RumbleHandler()
    {
        Preconditions.checkState(instance == null, "Only one instance of RumbleHandler is allowed");
        TickEvents.START_PLAYER.register(this::onUseItem);
        instance = this;
    }

    /**
     * Called when the player is damaged and rumbles the controller based on the amount of damage.
     *
     * @param player the player that was damaged
     * @param damage the amount of damage the player took
     */
    @ApiStatus.Internal
    public void onDamage(LocalPlayer player, float damage)
    {
        if(!Config.CLIENT.options.rumble.get())
            return;
        
        Controller controller = Controllable.getController();
        if(controller == null || !controller.isBeingUsed() || !controller.supportsRumble())
            return;

        float scale = damage / player.getMaxHealth();
        float maxFreq = Math.clamp(scale, 0.1F, 1.0F);
        float minFreq = Math.clamp(maxFreq - 0.2F, 0.1F, 1.0F);
        int time = Mth.clamp((int) (scale * 800), 100, 800);
        controller.rumble(minFreq, maxFreq, time);
    }

    /**
     * Tailors specific use animations with a custom controller rumble.
     *
     * @param player the player using the item
     */
    private void onUseItem(Player player)
    {
        if(!player.isLocalPlayer())
            return;

        if(!Config.CLIENT.options.rumble.get())
            return;

        Controller controller = Controllable.getController();
        if(controller == null || !controller.isBeingUsed() || !controller.supportsRumble())
            return;

        if(!player.isUsingItem())
            return;

        int ticks = player.getTicksUsingItem();
        ItemStack stack = player.getUseItem();
        switch(stack.getUseAnimation())
        {
            case EAT, DRINK -> {
                if(ticks >= 4) {
                    float maxFreq = Mth.abs(Mth.cos(ticks / 4F * (float) Math.PI) * 0.1F);
                    controller.rumble(0, maxFreq, 80);
                }
            }
            case BLOCK -> {
                float maxFreq = Math.min(ticks / 3F, 1F);
                maxFreq *= 0.15F;
                controller.rumble(0, maxFreq, 80);
            }
            case BOW -> {
                float maxFreq = ticks / 20F;
                maxFreq = (maxFreq * maxFreq + maxFreq * 2) / 3F;
                maxFreq = Mth.clamp(maxFreq, 0, 1);
                maxFreq *= 0.15F;
                controller.rumble(0, maxFreq, 80);
            }
            case SPEAR -> {
                float maxFreq = ticks / 10F;
                maxFreq = Mth.clamp(maxFreq, 0, 1);
                maxFreq *= 0.15F;
                controller.rumble(0, maxFreq, 80);
            }
            case CROSSBOW -> {
                if(ticks <= 24) {
                    float maxFreq = ticks / 20F;
                    maxFreq *= 0.15F;
                    controller.rumble(0, maxFreq, 80);
                }
            }
            case BRUSH -> {
                float maxFreq = (ticks % 5) / 5F;
                maxFreq = Mth.clamp(maxFreq, 0, 1);
                maxFreq *= 0.2F;
                controller.rumble(0, maxFreq, 80);
            }
        }
    }
}
