package com.mrcrayfish.controllable.mixin.client;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Author: MrCrayfish
 */
@Mixin(DeltaTracker.Timer.class)
public interface TimerAccessor
{
    @Accessor("deltaTickResidual")
    float controllable$DeltaTickResidual();
}
