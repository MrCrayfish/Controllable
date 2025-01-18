package com.mrcrayfish.controllable.client.binding.handlers.impl;

import com.mrcrayfish.controllable.client.binding.handlers.TickingHandler;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingReleased;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public final class DropHandler extends TickingHandler implements BindingReleased
{
    private int timer;
    private boolean dropped;

    @Override
    public TickPhase phase()
    {
        return TickPhase.START_CLIENT;
    }

    @Override
    public void handleTick(Context context)
    {
        if(context.screen().isEmpty())
        {
            context.controller().updateInputTime();
            if(!this.dropped && this.timer++ >= 10)
            {
                this.dropped = true;
                context.player().ifPresent(player -> {
                    if(!player.isSpectator()) {
                        player.drop(true);
                    }
                });
            }
        }
    }

    @Override
    public Optional<Runnable> createPressedHandler(Context context)
    {
        return Optional.of(() -> {
            if(context.screen().isEmpty()) {
                this.timer = 0;
                this.dropped = false;
            }
        });
    }

    @Override
    public boolean handleReleased(Context context)
    {
        if(context.screen().isEmpty())
        {
            if(this.timer < 10)
            {
                this.dropped = true;
                context.player().ifPresent(player -> {
                    player.drop(false);
                });
                context.controller().updateInputTime();
                return true;
            }
        }
        return false;
    }
}
