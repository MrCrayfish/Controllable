package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingReleased;

import java.util.Optional;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class OnPressAndReleaseHandler extends ButtonHandler implements BindingPressed, BindingReleased
{
    public static OnPressAndReleaseHandler create(Function<Context, Optional<Runnable>> pressHandler, Function<Context, Boolean> releaseHandler)
    {
        return new OnPressAndReleaseHandler()
        {
            @Override
            public Optional<Runnable> createPressedHandler(Context context)
            {
                return pressHandler.apply(context);
            }

            @Override
            public boolean handleReleased(Context context)
            {
                return releaseHandler.apply(context);
            }
        };
    }
}
