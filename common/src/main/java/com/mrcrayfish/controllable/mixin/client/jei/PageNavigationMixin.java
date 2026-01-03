package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.IconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(PageNavigation.class)
public interface PageNavigationMixin
{
    @Accessor(value = "nextButton", remap = false)
    IconButton controllableGetNextButton();

    @Accessor(value = "backButton", remap = false)
    IconButton controllableGetBackButton();
}
