package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.binding.IBindingContext;
import net.minecraftforge.client.settings.IKeyConflictContext;

/**
 * Author: MrCrayfish
 */
public class ForgeCompatBindingContext implements IBindingContext
{
    private final IKeyConflictContext context;

    public ForgeCompatBindingContext(IKeyConflictContext context)
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
        if(other instanceof ForgeCompatBindingContext forgeContext)
        {
            return this.context.conflicts(forgeContext.context);
        }
        return this.equals(other);
    }
}
