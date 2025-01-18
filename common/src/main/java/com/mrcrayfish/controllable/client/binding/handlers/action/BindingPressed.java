package com.mrcrayfish.controllable.client.binding.handlers.action;

import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public interface BindingPressed
{
    /**
     * Creates a runnable to execute or return empty if unable to run action. Returning a non-empty
     * optional indicates that the runnable be executed. For example,
     *
     * @param context
     * @return
     */
    Optional<Runnable> createPressedHandler(Context context);
}
