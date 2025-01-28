package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.recipes.RecipeGuiTab;
import mezz.jei.gui.recipes.RecipeGuiTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(RecipeGuiTabs.class)
public interface RecipeGuiTabsAccessor
{
    @Accessor(value = "tabs", remap = false)
    List<RecipeGuiTab> controllableGetTabs();

    @Accessor(value = "pageNavigation", remap = false)
    PageNavigation controllableGetNavigation();
}
