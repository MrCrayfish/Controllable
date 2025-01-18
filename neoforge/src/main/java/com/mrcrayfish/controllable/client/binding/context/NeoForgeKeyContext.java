package com.mrcrayfish.controllable.client.binding.context;

import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasPlayerRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasScreenRule;
import com.mrcrayfish.controllable.client.binding.context.rule.NoScreenRule;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.Set;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class NeoForgeKeyContext extends BindingContext
{
    private final IKeyConflictContext context;

    public NeoForgeKeyContext(IKeyConflictContext context)
    {
        super(generateId(context));
        this.context = context;
    }

    @Override
    public Set<ContextRule> createRules()
    {
        return generateRules(this.context);
    }

    @Override
    public int priority()
    {
        return 0;
    }

    private static ResourceLocation generateId(IKeyConflictContext context)
    {
        if(context == KeyConflictContext.UNIVERSAL)
        {
            return ResourceLocation.fromNamespaceAndPath("neoforge", "universal");
        }
        if(context == KeyConflictContext.GUI)
        {
            return ResourceLocation.fromNamespaceAndPath("neoforge", "gui");
        }
        if(context == KeyConflictContext.IN_GAME)
        {
            return ResourceLocation.fromNamespaceAndPath("neoforge", "in_game");
        }
        return ResourceLocation.fromNamespaceAndPath("neoforge", UUID.randomUUID().toString());
    }

    private static Set<ContextRule> generateRules(IKeyConflictContext context)
    {
        if(context == KeyConflictContext.GUI)
        {
            return Set.of(HasScreenRule.any());
        }
        if(context == KeyConflictContext.IN_GAME)
        {
            return Set.of(NoScreenRule.get(), HasPlayerRule.get());
        }
        return Set.of();
    }
}
