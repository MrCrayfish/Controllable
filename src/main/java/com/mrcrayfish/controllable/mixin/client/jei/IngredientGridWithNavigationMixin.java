package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
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
    PageNavigation getNavigation();

    @Accessor(value = "ingredientGrid", remap = false)
    IngredientGrid getIngredientGrid();
}
