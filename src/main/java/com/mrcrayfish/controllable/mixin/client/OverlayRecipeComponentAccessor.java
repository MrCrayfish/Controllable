package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

//TODO just use reflection

/**
 * Author: MrCrayfish
 */
@Mixin(OverlayRecipeComponent.class)
public interface OverlayRecipeComponentAccessor
{
    @Accessor("recipeButtons")
    List<OverlayRecipeComponent.OverlayRecipeButton> getRecipeButtons();
}
