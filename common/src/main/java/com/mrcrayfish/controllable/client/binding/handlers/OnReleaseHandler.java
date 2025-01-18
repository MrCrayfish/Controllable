package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingReleased;

import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class OnReleaseHandler extends ButtonHandler implements BindingReleased
{
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
