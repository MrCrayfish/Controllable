package com.mrcrayfish.controllable.client.binding.context.rule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collections;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public final class HasScreenRule extends ContextRule
{
    private final Set<Class<? extends Screen>> validScreens;

    private HasScreenRule(Set<Class<? extends Screen>> validScreens)
    {
        this.validScreens = Set.copyOf(validScreens);
    }

    @Override
    public boolean isActive()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen != null && (this.validScreens.isEmpty() || this.validScreens.contains(mc.screen.getClass()));
    }

    @Override
    public boolean matches(ContextRule other)
    {
        if(!(other instanceof HasScreenRule that))
            return false;
        if(this.validScreens.isEmpty())
            return true;
        if(that.validScreens.isEmpty())
            return true;
        return !Collections.disjoint(this.validScreens, that.validScreens);
    }

    public static HasScreenRule any()
    {
        return new HasScreenRule(Set.of());
    }

    public static HasScreenRule of(Set<Class<? extends Screen>> screenClasses)
    {
        return new HasScreenRule(screenClasses);
    }

    @Override
    public int hashCode()
    {
        // Enforces that only one type of screen rule can be added to binding contexts
        return HasScreenRule.class.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        // Enforces that only one type of screen rule can be added to binding contexts
        return obj != null && obj.getClass() == HasScreenRule.class;
    }
}
