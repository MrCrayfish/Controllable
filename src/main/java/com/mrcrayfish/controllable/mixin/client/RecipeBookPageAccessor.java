package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.GuiButtonRecipe;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor
{
    @Accessor("buttons")
    List<GuiButtonRecipe> getButtons();

    @Accessor("forwardButton")
    GuiButtonToggle getForwardButton();

    @Accessor("backButton")
    GuiButtonToggle getBackButton();

    @Accessor("currentPage")
    int getCurrentPage();

    @Accessor("currentPage")
    void setCurrentPage(int page);

    @Invoker("updateButtonsForPage")
    void invokeUpdateButtonsForPage();
}
