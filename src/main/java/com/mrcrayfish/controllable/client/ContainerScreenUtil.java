package com.mrcrayfish.controllable.client;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ContainerScreenUtil
{

    private static Method handleMouseClickMethod;

    public static void handleMouseClick(ContainerScreen<?> containerScreen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if (handleMouseClickMethod == null)
        {
            handleMouseClickMethod = ObfuscationReflectionHelper.findMethod(containerScreen.getClass(), "func_184098_a", Slot.class, int.class, int.class, ClickType.class);
        }

        try
        {
            handleMouseClickMethod.invoke(containerScreen, slotIn, slotId, mouseButton, type);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }

    }

}
