package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(CreativeScreen.class)
public interface CreativeScreenMixin
{
    @Accessor("tabPage")
    static int getTabPage() {
        throw new AssertionError();
    }
}
