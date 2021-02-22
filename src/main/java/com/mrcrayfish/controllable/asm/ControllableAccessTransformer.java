package com.mrcrayfish.controllable.asm;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

/**
 * Author: MrCrayfish
 */
public class ControllableAccessTransformer extends AccessTransformer
{
    public ControllableAccessTransformer() throws IOException
    {
        super("controllable_at.cfg");
    }
}
