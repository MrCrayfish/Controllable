package com.mrcrayfish.controllable.client.binding.context.rule;

import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public final class HasPlayerRule extends ContextRule
{
    private static final HasPlayerRule INSTANCE = new HasPlayerRule();

    private HasPlayerRule() {}

    @Override
    public boolean isActive()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null;
    }

    @Override
    public boolean matches(ContextRule other)
    {
        return other == INSTANCE;
    }

    public static HasPlayerRule get()
    {
        return INSTANCE;
    }
}
