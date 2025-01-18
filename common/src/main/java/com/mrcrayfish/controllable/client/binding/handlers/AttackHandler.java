package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.api.client.binding.handlers.OnPressHandler;
import com.mrcrayfish.controllable.api.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.platform.ClientServices;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class AttackHandler extends OnPressHandler
{
    private static boolean preventContinue;

    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                if(!player.isUsingItem()) {
                    preventContinue = ClientServices.CLIENT.startAttack(context.minecraft());
                }
            });
        });
    }

    public static boolean shouldPreventContinue()
    {
        boolean result = preventContinue;
        preventContinue = false;
        return result;
    }
}
