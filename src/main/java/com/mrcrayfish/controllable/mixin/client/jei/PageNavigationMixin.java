package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.GuiIconButton;
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
    GuiIconButton getNextButton();

    @Accessor(value = "backButton", remap = false)
    GuiIconButton getBackButton();
}
