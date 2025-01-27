package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingReleased;

import java.util.Optional;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class OnReleaseHandler extends ButtonHandler implements BindingPressed, BindingReleased
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {});
    }

    public static OnReleaseHandler create(Function<Context, Boolean> handler)
    {
        return new OnReleaseHandler()
        {
            @Override
            public boolean handleReleased(Context context)
            {
                return handler.apply(context);
            }
        };
    }
}
