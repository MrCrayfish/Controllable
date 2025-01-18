package com.mrcrayfish.controllable.client.binding.context;

import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasPlayerRule;
import com.mrcrayfish.controllable.client.binding.context.rule.NoScreenRule;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class InGameContext extends BindingContext
{
    public static final InGameContext INSTANCE = new InGameContext(Utils.resource("in_game"));

    protected InGameContext(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public Set<ContextRule> createRules()
    {
        return Set.of(NoScreenRule.get(), HasPlayerRule.get());
    }

    @Override
    public int priority()
    {
        return 0;
    }
}
