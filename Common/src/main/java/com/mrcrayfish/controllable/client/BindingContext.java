package com.mrcrayfish.controllable.client;

import net.minecraft.client.Minecraft;

/**
 * Author: MrCrayfish
 */
public enum BindingContext implements IBindingContext
{
    GLOBAL {
        @Override
        public boolean isActive()
        {
            return true;
        }

        @Override
        public boolean conflicts(IBindingContext context)
        {
            return true;
        }
    },
    IN_GAME {
        @Override
        public boolean isActive()
        {
            return !IN_SCREEN.isActive();
        }

        @Override
        public boolean conflicts(IBindingContext other)
        {
            return this == other;
        }
    },
    IN_SCREEN {
        @Override
        public boolean isActive()
        {
            return Minecraft.getInstance().screen != null;
        }

        @Override
        public boolean conflicts(IBindingContext context)
        {
            return this == context;
        }
    };

    @Override
    public boolean isActive()
    {
        return false;
    }

    @Override
    public boolean conflicts(IBindingContext context)
    {
        return false;
    }
}
