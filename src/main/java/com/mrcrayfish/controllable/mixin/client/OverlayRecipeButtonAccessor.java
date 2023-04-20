package com.mrcrayfish.controllable.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton")
public interface OverlayRecipeButtonAccessor
{
    @Accessor("isCraftable")
    boolean controllableIsCraftable();
}
