package com.mrcrayfish.controllable.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ControllableMixinPlugin implements IMixinConfigPlugin
{
    private boolean frameworkLoaded;

    @Override
    public void onLoad(String mixinPackage)
    {
        this.frameworkLoaded = this.isClassAvailable("com.mrcrayfish.framework.Constants");
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        // Prevent any loading of mixins if Framework is not installed.
        return this.frameworkLoaded;
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
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }

    private boolean isClassAvailable(String className)
    {
        try
        {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
