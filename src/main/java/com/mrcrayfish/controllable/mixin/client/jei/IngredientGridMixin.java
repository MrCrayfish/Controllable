package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.render.IngredientListBatchRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(IngredientGrid.class)
public interface IngredientGridMixin
{
    @Accessor(value = "guiIngredientSlots", remap = false)
    IngredientListBatchRenderer getGuiIngredientSlots();
}
