package com.mrcrayfish.controllable.client.binding.context.rule;

import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public final class NoScreenRule extends ContextRule
{
    private static final NoScreenRule INSTANCE = new NoScreenRule();

    private NoScreenRule() {}

    @Override
    public boolean isActive()
    {
        return Minecraft.getInstance().screen == null;
    }

    @Override
    public boolean matches(ContextRule other)
    {
        return other == INSTANCE;
    }

    public static NoScreenRule get()
    {
        return INSTANCE;
    }
}
