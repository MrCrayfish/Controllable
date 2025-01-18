package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public final class EmptyHandler extends OnPressHandler
{
    public static final EmptyHandler INSTANCE = new EmptyHandler();

    private EmptyHandler() {}

    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {});
    }
}
