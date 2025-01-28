package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.recipes.RecipeCatalysts;
import mezz.jei.gui.recipes.RecipeGuiTabs;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

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

    @Accessor(value = "recipeTransferButtons", remap = false)
    List<RecipeTransferButton> controllableRecipeTransferButtons();

    @Accessor(value = "recipeLayouts", remap = false)
    List<IRecipeLayoutDrawable<?>> controllableGetLayouts();
}
