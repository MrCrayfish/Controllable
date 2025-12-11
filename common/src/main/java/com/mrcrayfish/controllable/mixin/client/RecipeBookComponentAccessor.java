package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.components.CycleButton;
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
public interface RecipeBookComponentAccessor
{
    @Accessor("filterButton")
    CycleButton<Boolean> controllableGetFilterButton();

    @Accessor("tabButtons")
    List<RecipeBookTabButton> controllableGetRecipeTabs();

    @Accessor("recipeBookPage")
    RecipeBookPage controllableGetRecipeBookPage();

    @Accessor("selectedTab")
    RecipeBookTabButton controllableGetCurrentTab();

    @Accessor("selectedTab")
    void controllableSetCurrentTab(RecipeBookTabButton tab);

    @Invoker("updateCollections")
    void controllableUpdateCollections(boolean resetPages, boolean filtering);

    @Accessor("tabButtons")
    List<RecipeBookTabButton> controllableGetTabButtons();

    @Invoker("isFiltering")
    boolean controllableIsFiltering();
}
