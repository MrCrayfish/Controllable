package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.recipes.RecipeCatalysts;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipeGuiTabs;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(RecipesGui.class)
public interface RecipesGuiAccessor
{
    @Accessor(value = "recipeCatalysts", remap = false)
    RecipeCatalysts controllableGetCatalysts();

    @Accessor(value = "recipeGuiTabs", remap = false)
    RecipeGuiTabs controllableGetRecipeGuiTabs();

    @Accessor(value = "layouts", remap = false)
    RecipeGuiLayouts controllableGetLayouts();
}
