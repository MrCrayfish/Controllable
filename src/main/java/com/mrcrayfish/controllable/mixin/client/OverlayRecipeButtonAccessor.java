package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//TODO just use reflection

/**
 * Author: MrCrayfish
 */
@Mixin(OverlayRecipeComponent.OverlayRecipeButton.class)
public interface OverlayRecipeButtonAccessor
{
    @Accessor("isCraftable")
    boolean isCraftable();
}
