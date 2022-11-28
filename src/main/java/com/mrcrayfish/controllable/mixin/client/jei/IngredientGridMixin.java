package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.common.gui.overlay.IngredientGrid;
import mezz.jei.common.render.IngredientListRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Yes this is terrible. You should never Mixin to other mods. Do I expect mezz to
 * support Controllable? No. This is the terrible solution we have to live with.
 *
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(IngredientGrid.class)
public interface IngredientGridMixin
{
    @Accessor(value = "ingredientListRenderer", remap = false)
    IngredientListRenderer getIngredientListRenderer();
}
