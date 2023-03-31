package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public interface IBindingContext
{
    boolean isActive();

    boolean conflicts(IBindingContext context);
}
