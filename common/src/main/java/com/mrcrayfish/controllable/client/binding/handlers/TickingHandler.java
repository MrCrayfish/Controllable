package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingOnTick;

import java.util.Optional;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class TickingHandler extends ButtonHandler implements BindingPressed, BindingOnTick
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {});
    }

    public static TickingHandler create(TickPhase phase, Function<Context, Boolean> handler)
    {
        return new TickingHandler()
        {
            @Override
            public TickPhase phase()
            {
                return phase;
            }

            @Override
            public void handleTick(Context context)
            {
                handler.apply(context);
            }
        };
    }
}
