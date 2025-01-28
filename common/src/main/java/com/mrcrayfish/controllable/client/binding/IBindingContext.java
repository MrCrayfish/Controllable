package com.mrcrayfish.controllable.client.binding;

/**
 * Author: MrCrayfish
 */
public interface IBindingContext
{
    boolean isActive();

    boolean conflicts(IBindingContext context);
}
