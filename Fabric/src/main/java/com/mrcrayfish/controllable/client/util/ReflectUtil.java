package com.mrcrayfish.controllable.client.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

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
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_TOP = ReflectUtil.findMethod(AbstractSelectionList.class, "net.minecraft.class_350", "method_25337", "(I)I", int.class);
    private static final Method ABSTRACT_SELECTION_LIST_GET_ROW_BOTTOM = ReflectUtil.findMethod(AbstractSelectionList.class, "net.minecraft.class_350", "method_25319", "(I)I", int.class);
    private static final Method ABSTRACT_CONTAINER_SCREEN_CLICK_SLOT = ReflectUtil.findMethod(AbstractContainerScreen.class, "net.minecraft.class_465", "method_2383", "(Lnet/minecraft/class_1735;IILnet/minecraft/class_1713;)V", Slot.class, int.class, int.class, ClickType.class);
    private static final Method SCREEN_ADD_RENDER_WIDGET = ReflectUtil.findMethod(Screen.class, "net.minecraft.class_437", "method_37060", "(Lnet/minecraft/class_4068;)Lnet/minecraft/class_4068;", Renderable.class);
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ReflectUtil.findField(AbstractSelectionList.class, "net.minecraft.class_350", "field_22741", "I");
    private static final Field IMAGE_BUTTON_RESOURCE = ReflectUtil.findField(ImageButton.class, "net.minecraft.class_344", "field_2127", "Lnet/minecraft/class_2960;");
    private static final Field CREATIVE_SCREEN_SCROLL_OFFSET = ReflectUtil.findField(CreativeModeInventoryScreen.class, "net.minecraft.class_481", "field_2890", "F");
    private static final Field KEY_MAPPING_PRESS_TIME = ReflectUtil.findField(KeyMapping.class, "net.minecraft.class_304", "field_1661", "I");
    private static final Field TOOLTIP_LINES = ReflectUtil.findField(Tooltip.class, "net.minecraft.class_7919", "field_41103", "Ljava/util/List;");
    private static final Field STONE_CUTTER_INDEX = ReflectUtil.findField(StonecutterScreen.class, "net.minecraft.class_3979", "field_17671", "I");
    private static final Field LOOM_START_ROW = ReflectUtil.findField(LoomScreen.class, "net.minecraft.class_494", "field_39190", "I");

    private static Method findMethod(Class<?> targetClass, String className, String methodName, String methodDesc, Class<?>... types)
    {
        try
        {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            Method method = targetClass.getDeclaredMethod(resolver.mapMethodName("intermediary", className, methodName, methodDesc), types);
            method.setAccessible(true);
            return method;
        }
        catch(NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> targetClass, String className, String fieldName, String fieldDesc)
    {
        try
        {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            Field field = targetClass.getDeclaredField(resolver.mapFieldName("intermediary", className, fieldName, fieldDesc));
            field.setAccessible(true);
            return field;
        }
        catch(NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

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
        try
        {
            TOOLTIP_LINES.set(tooltip, lines);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
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

    public static void addRenderable(Screen screen, Renderable renderable)
    {
        try
        {
            SCREEN_ADD_RENDER_WIDGET.invoke(screen, renderable);
        }
        catch(InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public static int getLoomStartRow(LoomScreen screen)
    {
        try
        {
            return (int) LOOM_START_ROW.get(screen);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
