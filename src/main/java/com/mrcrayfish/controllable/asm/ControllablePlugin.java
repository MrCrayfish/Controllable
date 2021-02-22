package com.mrcrayfish.controllable.asm;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@IFMLLoadingPlugin.TransformerExclusions({"com.mrcrayfish.controllable.asm"})
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Controllable")
public class ControllablePlugin implements IFMLLoadingPlugin
{
    public static File location = null;

    public ControllablePlugin()
    {
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("controllable.mixins.json");
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {};
    }

    @Override
    public String getModContainerClass()
    {
        return Controllable.class.getName();
    }

    @Nullable
    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        if((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
        {
            try
            {
                location = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                /* I believe ForgeGradle 3.0 changed the out directory, classes and resources are
                 * separate now so this fixes resources not being loaded in a dev environment. */
                location = new File(location.getParentFile(), "resources");
            }
            catch(URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            location = (File) data.get("coremodLocation");
        }
    }

    @Override
    public String getAccessTransformerClass()
    {
        return ControllableAccessTransformer.class.getName();
    }
}
