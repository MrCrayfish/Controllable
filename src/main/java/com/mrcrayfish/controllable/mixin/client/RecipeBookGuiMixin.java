package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeTabToggleWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookGui.class)
public interface RecipeBookGuiMixin
{
    @Accessor("toggleRecipesBtn")
    ToggleWidget getToggleRecipesBtn();

    @Accessor("recipeTabs")
    List<RecipeTabToggleWidget> getRecipeTabs();

    @Accessor("recipeBookPage")
    RecipeBookPage getRecipeBookPage();
}
