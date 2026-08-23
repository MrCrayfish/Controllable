package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(IngredientGridWithNavigation.class)
public interface IngredientGridWithNavigationMixin
{
    @Accessor(value = "navigation", remap = false)
    PageNavigation controllableGetNavigation();

    @Accessor(value = "ingredientGrid", remap = false)
    IngredientGrid controllableGetIngredientGrid();
}
