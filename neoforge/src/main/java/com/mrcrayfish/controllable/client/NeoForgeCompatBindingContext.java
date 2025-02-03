package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.binding.IBindingContext;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

/**
 * Author: MrCrayfish
 */
public class NeoForgeCompatBindingContext implements IBindingContext
{
    private final IKeyConflictContext context;

    public NeoForgeCompatBindingContext(IKeyConflictContext context)
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
        if(other instanceof NeoForgeCompatBindingContext forgeContext)
        {
            return this.context.conflicts(forgeContext.context);
        }
        return this.equals(other);
    }
}
