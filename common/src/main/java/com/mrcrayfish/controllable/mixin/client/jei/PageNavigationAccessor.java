package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.GuiIconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(PageNavigation.class)
public interface PageNavigationAccessor
{
    @Accessor(value = "nextButton", remap = false)
    GuiIconButton controllableGetNextButton();

    @Accessor(value = "backButton", remap = false)
    GuiIconButton controllableGetBackButton();

    @Invoker(value = "isVisible", remap = false)
    boolean controllableIsVisible();
}
