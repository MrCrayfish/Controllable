package com.mrcrayfish.controllable.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ControllableMixinPlugin implements IMixinConfigPlugin
{
    private boolean optifineLoaded;

    @Override
    public void onLoad(String mixinPackage)
    {
        try
        {
            Class.forName("optifine.Installer");
            this.optifineLoaded = true;
        }
        catch (ClassNotFoundException e)
        {
            this.optifineLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        return this.optifineLoaded ? !mixinClassName.equals("com.mrcrayfish.controllable.mixin.client.GameRendererMixin") : !mixinClassName.equals("com.mrcrayfish.controllable.mixin.client.OptifineGameRendererMixin");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {

    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }
}
