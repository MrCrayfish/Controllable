package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.api.client.binding.handlers.MovementInputHandler;
import com.mrcrayfish.controllable.api.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.api.client.binding.handlers.action.context.MovementInputContext;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class SneakHandler extends MovementInputHandler
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() ->
        {
            if(context.minecraft().options.toggleCrouch().get())
            {
                context.minecraft().options.keyShift.setDown(true);
            }
        });
    }

    @Override
    public void handleMovementInput(MovementInputContext context)
    {
        if(!context.minecraft().options.toggleCrouch().get())
        {
            context.input().shiftKeyDown = true;
            context.controller().updateInputTime();
        }
    }
}
