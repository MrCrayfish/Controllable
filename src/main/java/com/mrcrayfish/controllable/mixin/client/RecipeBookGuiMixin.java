package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.GuiButtonRecipeTab;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(GuiRecipeBook.class)
public interface RecipeBookGuiMixin
{
    @Accessor("toggleRecipesBtn")
    GuiButtonToggle getToggleRecipesBtn();

    @Accessor("recipeTabs")
    List<GuiButtonRecipeTab> getRecipeTabs();

    @Accessor("recipeBookPage")
    RecipeBookPage getRecipeBookPage();

    @Accessor("currentTab")
    GuiButtonRecipeTab getCurrentTab();

    @Accessor("currentTab")
    void setCurrentTab(GuiButtonRecipeTab tab);

    @Invoker("updateCollections")
    void invokeUpdateCollections(boolean resetPages);
}
