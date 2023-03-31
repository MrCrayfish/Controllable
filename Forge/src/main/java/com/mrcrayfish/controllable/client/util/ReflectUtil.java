package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ReflectUtil
{
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_TOP = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_7610_", int.class);
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_BOTTOM = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_93485_", int.class);
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93387_");
    private static final Field IMAGE_BUTTON_RESOURCE = ObfuscationReflectionHelper.findField(ImageButton.class, "f_94223_");

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

    public static int getAbstractListItemHeight(AbstractSelectionList<?> list)
    {
        try
        {
            return (int) ABSTRACT_SELECTION_LIST_ITEM_HEIGHT.get(list);
        }
        catch(IllegalAccessException e)
        {
            return 10;
        }
    }

    @Nullable
    public static ResourceLocation getImageButtonResource(ImageButton button)
    {
        try
        {
            return (ResourceLocation) IMAGE_BUTTON_RESOURCE.get(button);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public static void pushLinesToTooltip(Tooltip tooltip, List<FormattedCharSequence> lines)
    {
        ObfuscationReflectionHelper.setPrivateValue(Tooltip.class, tooltip, lines, "f_256766_");
    }
}
