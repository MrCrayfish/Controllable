package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.widget.ToggleWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookGui.class)
public interface RecipeBookGuiMixin
{
    @Accessor("toggleRecipesBtn")
    ToggleWidget getToggleRecipesBtn();

    @Accessor("recipeBookPage")
    RecipeBookPage getRecipeBookPage();
}
