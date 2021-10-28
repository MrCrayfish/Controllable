package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
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
    List<RecipeButton> getButtons();

    @Accessor("forwardButton")
    StateSwitchingButton getForwardButton();

    @Accessor("backButton")
    StateSwitchingButton getBackButton();

    @Accessor("currentPage")
    int getCurrentPage();

    @Accessor("currentPage")
    void setCurrentPage(int page);

    @Invoker("updateButtonsForPage")
    void invokeUpdateButtonsForPage();
}
