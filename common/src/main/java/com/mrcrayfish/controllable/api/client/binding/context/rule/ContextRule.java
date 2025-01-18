package com.mrcrayfish.controllable.api.client.binding.context.rule;

/**
 * Author: MrCrayfish
 */
public abstract sealed class ContextRule permits HasPlayerRule, NoScreenRule, HasScreenRule
{
    public abstract boolean isActive();

    public abstract boolean matches(ContextRule other);
}
