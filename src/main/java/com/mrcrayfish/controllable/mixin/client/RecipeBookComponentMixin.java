package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentMixin
{
    @Accessor("filterButton")
    StateSwitchingButton getFilterButton();

    @Accessor("tabButtons")
    List<RecipeBookTabButton> getRecipeTabs();

    @Accessor("recipeBookPage")
    RecipeBookPage getRecipeBookPage();

    @Accessor("selectedTab")
    RecipeBookTabButton getCurrentTab();

    @Accessor("selectedTab")
    void setCurrentTab(RecipeBookTabButton tab);

    @Invoker("updateCollections")
    void invokeUpdateCollections(boolean resetPages);
}
