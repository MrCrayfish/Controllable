package com.mrcrayfish.controllable.client.binding.context;

import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasPlayerRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasScreenRule;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class InGameWithScreenContext extends InScreenContext
{
    public static final InGameWithScreenContext INSTANCE = new InGameWithScreenContext(Utils.resource("in_game_with_screen"));

    protected InGameWithScreenContext(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public Set<ContextRule> createRules()
    {
        return Set.of(HasScreenRule.any(), HasPlayerRule.get());
    }
}
