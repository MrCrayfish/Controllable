package com.mrcrayfish.controllable.client.binding.context;

import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasScreenRule;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class InScreenContext extends BindingContext
{
    public static final InScreenContext INSTANCE = new InScreenContext(Utils.resource("in_screen"));

    protected InScreenContext(Identifier id)
    {
        super(id);
    }

    @Override
    public Set<ContextRule> createRules()
    {
        return Set.of(HasScreenRule.any());
    }

    @Override
    public int priority()
    {
        return 0;
    }
}
