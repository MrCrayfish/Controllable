package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenMixin
{
    @Accessor(value = "tabPage", remap = false)
    static int getTabPage() {
        throw new AssertionError();
    }
}
