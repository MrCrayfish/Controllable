package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(GuiContainerCreative.class)
public interface CreativeScreenMixin
{
    @Accessor(value = "tabPage", remap = false)
    static int getTabPage() {
        throw new AssertionError();
    }
}
