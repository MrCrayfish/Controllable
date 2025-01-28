package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.recipes.RecipeGuiTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(RecipeGuiTab.class)
public interface RecipeGuiTabAccessor
{
    @Accessor(value = "area", remap = false)
    ImmutableRect2i controllableGetArea();
}
