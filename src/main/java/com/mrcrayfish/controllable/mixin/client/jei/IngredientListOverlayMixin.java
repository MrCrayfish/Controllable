package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(IngredientListOverlay.class)
public interface IngredientListOverlayMixin
{
    @Accessor(value = "contents", remap = false)
    IngredientGridWithNavigation getContents();
}
