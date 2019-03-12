package com.mrcrayfish.controllable.asm;

import com.mrcrayfish.controllable.Controllable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@IFMLLoadingPlugin.TransformerExclusions({"com.mrcrayfish.controllable.asm"})
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Controllable")
public class ControllablePlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {"com.mrcrayfish.controllable.asm.ControllableTransformer"};
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
