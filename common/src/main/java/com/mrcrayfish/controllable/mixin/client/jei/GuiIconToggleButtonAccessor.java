package com.mrcrayfish.controllable.mixin.client.jei;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Pseudo
@Mixin(GuiIconToggleButton.class)
public interface GuiIconToggleButtonAccessor
{
    @Accessor(value = "button", remap = false)
    GuiIconButton controllableGetButton();

    @Accessor(value = "area", remap = false)
    ImmutableRect2i controllableGetArea();
}
