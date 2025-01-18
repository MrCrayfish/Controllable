package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;

import java.util.Optional;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class OnPressHandler extends ButtonHandler implements BindingPressed
{
    public static OnPressHandler create(Function<Context, Optional<Runnable>> handler)
    {
        return new OnPressHandler()
        {
            @Override
            public Optional<Runnable> createPressedHandler(Context context)
            {
                return handler.apply(context);
            }
        };
    }
}
