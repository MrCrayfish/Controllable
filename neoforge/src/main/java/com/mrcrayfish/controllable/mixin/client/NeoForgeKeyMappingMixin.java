package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Author: MrCrayfish
 */
@Mixin(KeyMapping.class)
public abstract class NeoForgeKeyMappingMixin implements IKeyMappingExtension
{
    @Shadow @Final
    private String name;

    @Override
    public boolean isActiveAndMatches(InputConstants.Key key)
    {
        String customKey = this.name + ".custom";
        KeyAdapterBinding adapter = Controllable.getBindingRegistry().getKeyAdapters().get(customKey);
        if(adapter != null && adapter.isButtonDown())
        {
            return true;
        }
        return key != InputConstants.UNKNOWN && key.equals(getKey()) && this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
    }
}
