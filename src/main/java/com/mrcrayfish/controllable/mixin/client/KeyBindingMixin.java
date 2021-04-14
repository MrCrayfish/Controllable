package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Author: MrCrayfish
 */
@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements IForgeKeybinding
{
    @Shadow
    public abstract String getKeyDescription();

    @Override
    public boolean isActiveAndMatches(InputMappings.Input keyCode)
    {
        String customKey = this.getKeyDescription() + ".custom";
        KeyAdapterBinding adapter = BindingRegistry.getInstance().getKeyAdapters().get(customKey);
        if(adapter != null && adapter.isButtonDown())
        {
            return true;
        }
        return keyCode != InputMappings.INPUT_INVALID && keyCode.equals(getKey()) && getKeyConflictContext().isActive() && getKeyModifier().isActive(getKeyConflictContext());
    }
}
