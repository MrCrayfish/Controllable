package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.RenderingContext;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingOnRender;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class RenderingHandler extends ButtonHandler implements BindingPressed, BindingOnRender
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {});
    }

    public static RenderingHandler create(RenderPhase phase, Consumer<RenderingContext> handler)
    {
        return new RenderingHandler()
        {
            @Override
            public RenderPhase phase()
            {
                return phase;
            }

            @Override
            public void handleRender(RenderingContext context)
            {
                handler.accept(context);
            }
        };
    }
}
