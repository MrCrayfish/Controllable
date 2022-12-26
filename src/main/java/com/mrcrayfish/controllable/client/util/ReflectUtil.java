package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
public class ReflectUtil
{
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_TOP = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_7610_", int.class);
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_BOTTOM = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_93485_", int.class);

    public static int getAbstractListRowTop(AbstractSelectionList<?> list, int index)
    {
        try
        {
            return (int) ABSTRACT_SELECTION_LIST_GET_ROW_TOP.invoke(list, index);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
            return 1000000;
        }
    }

    public static int getAbstractListRowBottom(AbstractSelectionList<?> list, int index)
    {
        try
        {
            return (int) ABSTRACT_SELECTION_LIST_GET_ROW_BOTTOM.invoke(list, index);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
            return -1000000;
        }
    }

    public static int getListItemHeight(AbstractSelectionList<?> list)
    {
        try
        {
            Field field = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93387_");
            field.setAccessible(true);
            return (int) field.get(list);
        }
        catch(IllegalAccessException e)
        {
            return 10;
        }
    }
}
