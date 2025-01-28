package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeGuiLayouts.class)
public interface RecipeGuiLayoutsAccessor
{
    @Accessor(value = "recipeLayoutsWithButtons", remap = false)
    List<RecipeLayoutWithButtons<?>> controllableGetRecipeLayouts();
}
