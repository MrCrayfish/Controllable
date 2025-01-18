package com.mrcrayfish.controllable.client.binding.handlers;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.MovementInputContext;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingMovementInput;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public abstract non-sealed class MovementInputHandler extends ButtonHandler implements BindingPressed, BindingMovementInput
{
    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        // Must capture on pressed for movement input to be run
        return Optional.of(() -> {});
    }

    public static MovementInputHandler create(Consumer<MovementInputContext> inputHandler)
    {
        return new MovementInputHandler()
        {
            @Override
            public void handleMovementInput(MovementInputContext context)
            {
                inputHandler.accept(context);
            }
        };
    }
}
