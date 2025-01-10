package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.binding.IBindingContext;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

/**
 * Author: MrCrayfish
 */
public class NeoForgeBindingContext implements IBindingContext
{
    private final IKeyConflictContext context;

    public NeoForgeBindingContext(IKeyConflictContext context)
    {
        this.context = context;
    }

    @Override
    public boolean isActive()
    {
        return this.context.isActive();
    }

    @Override
    public boolean conflicts(IBindingContext other)
    {
        if(other instanceof NeoForgeBindingContext forgeContext)
        {
            return this.context.conflicts(forgeContext.context);
        }
        return this.equals(other);
    }
}
