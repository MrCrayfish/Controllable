package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
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
    List<RecipeWidget> getButtons();

    @Accessor("forwardButton")
    ToggleWidget getForwardButton();

    @Accessor("backButton")
    ToggleWidget getBackButton();

    @Accessor("currentPage")
    int getCurrentPage();

    @Accessor("currentPage")
    void setCurrentPage(int page);

    @Invoker("updateButtonsForPage")
    void invokeUpdateButtonsForPage();
}
