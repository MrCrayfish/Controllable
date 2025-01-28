package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
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
    private static final Method ABSTRACT_CONTAINER_SCREEN_CLICK_SLOT = ObfuscationReflectionHelper.findMethod(AbstractContainerScreen.class, "m_6597_", Slot.class, int.class, int.class, ClickType.class);
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93387_");
    private static final Field IMAGE_BUTTON_RESOURCE = ObfuscationReflectionHelper.findField(ImageButton.class, "f_94223_");
    private static final Field CREATIVE_SCREEN_SCROLL_OFFSET = ObfuscationReflectionHelper.findField(CreativeModeInventoryScreen.class, "f_98508_");
    private static final Field KEY_MAPPING_PRESS_TIME = ObfuscationReflectionHelper.findField(KeyMapping.class, "f_90818_");
    private static final Field STONE_CUTTER_INDEX = ObfuscationReflectionHelper.findField(StonecutterScreen.class, "f_99306_");
    private static final Field LOOM_START_ROW = ObfuscationReflectionHelper.findField(LoomScreen.class, "f_232823_");

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

    public static float getCreativeScrollOffset(CreativeModeInventoryScreen screen)
    {
        try
        {
            return (float) CREATIVE_SCREEN_SCROLL_OFFSET.get(screen);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void setCreativeScrollOffset(CreativeModeInventoryScreen screen, float offset)
    {
        try
        {
            CREATIVE_SCREEN_SCROLL_OFFSET.set(screen, offset);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void setKeyPressTime(KeyMapping mapping, int value)
    {
        try
        {
            KEY_MAPPING_PRESS_TIME.set(mapping, value);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void clickSlot(AbstractContainerScreen<?> screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        try
        {
            ABSTRACT_CONTAINER_SCREEN_CLICK_SLOT.invoke(screen, slotIn, slotId, mouseButton, type);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public static int getStonecutterStartIndex(StonecutterScreen screen)
    {
        try
        {
            return (int) STONE_CUTTER_INDEX.get(screen);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getLoomStartRow(LoomScreen screen)
    {
        try
        {
            return (int) LOOM_START_ROW.get(screen);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}
