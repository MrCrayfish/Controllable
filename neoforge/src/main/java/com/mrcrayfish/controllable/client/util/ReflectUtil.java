package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ReflectUtil
{
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_TOP = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "getRowTop", int.class);
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_BOTTOM = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "getRowBottom", int.class);
    private static final Method ABSTRACT_CONTAINER_SCREEN_CLICK_SLOT = ObfuscationReflectionHelper.findMethod(AbstractContainerScreen.class, "slotClicked", Slot.class, int.class, int.class, ClickType.class);
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "itemHeight");
    private static final Field CREATIVE_SCREEN_SCROLL_OFFSET = ObfuscationReflectionHelper.findField(CreativeModeInventoryScreen.class, "scrollOffs");
    private static final Field KEY_MAPPING_PRESS_TIME = ObfuscationReflectionHelper.findField(KeyMapping.class, "clickCount");
    private static final Field STONE_CUTTER_INDEX = ObfuscationReflectionHelper.findField(StonecutterScreen.class, "startIndex");
    private static final Field LOOM_START_ROW = ObfuscationReflectionHelper.findField(LoomScreen.class, "startRow");
    private static final Field IMAGE_BUTTON_SPRITES = ObfuscationReflectionHelper.findField(ImageButton.class, "sprites");

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

    public static void pushLinesToTooltip(Tooltip tooltip, List<FormattedCharSequence> lines)
    {
        ObfuscationReflectionHelper.setPrivateValue(Tooltip.class, tooltip, lines, "cachedTooltip");
        ObfuscationReflectionHelper.setPrivateValue(Tooltip.class, tooltip, Language.getInstance(), "splitWithLanguage");
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

    @Nullable
    public static WidgetSprites getImageButtonResource(ImageButton button)
    {
        try
        {
            return (WidgetSprites) IMAGE_BUTTON_SPRITES.get(button);
        }
        catch(IllegalAccessException e)
        {
            return null;
        }
    }
}
