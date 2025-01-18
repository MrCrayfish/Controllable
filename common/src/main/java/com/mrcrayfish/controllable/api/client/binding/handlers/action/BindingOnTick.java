package com.mrcrayfish.controllable.api.client.binding.handlers.action;

import com.mrcrayfish.controllable.api.client.binding.handlers.action.context.Context;

/**
 * Author: MrCrayfish
 */
public interface BindingOnTick
{
    TickPhase phase();

    void handleTick(Context context);

    enum TickPhase
    {
        START_CLIENT,
        END_CLIENT,
        START_PLAYER,
        END_PLAYER
    }
}
