package com.mrcrayfish.controllable.api.client.binding.handlers.action;

import com.mrcrayfish.controllable.api.client.binding.handlers.action.context.RenderingContext;

/**
 * Author: MrCrayfish
 */
public interface BindingOnRender
{
    RenderPhase phase();

    void handleRender(RenderingContext context);

    enum RenderPhase
    {
        START_RENDER,
        END_RENDER
    }
}
