package com.mrcrayfish.controllable.client.binding.context;

import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 *
 *
 * Author: MrCrayfish
 */
public class GlobalContext extends BindingContext
{
    public static final GlobalContext INSTANCE = new GlobalContext(Utils.resource("global"));

    protected GlobalContext(ResourceLocation id)
    {
        super(id);
    }

    @Override
    protected Set<ContextRule> createRules()
    {
        return Set.of();
    }

    @Override
    public int priority()
    {
        return 0;
    }
}
