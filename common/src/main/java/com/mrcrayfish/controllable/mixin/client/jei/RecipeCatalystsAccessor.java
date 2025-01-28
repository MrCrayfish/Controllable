package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.gui.recipes.RecipeCatalysts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(RecipeCatalysts.class)
public interface RecipeCatalystsAccessor
{
    @Accessor(value = "recipeSlots", remap = false)
    List<IRecipeSlotDrawable> controllableGetRecipeSlots();
}
