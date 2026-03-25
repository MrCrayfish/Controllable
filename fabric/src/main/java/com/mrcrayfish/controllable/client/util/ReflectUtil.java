package com.mrcrayfish.controllable.client.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
public class ReflectUtil
{
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ReflectUtil.findField(AbstractSelectionList.class, "defaultEntryHeight");
    private static final Field IMAGE_BUTTON_SPRITES = ReflectUtil.findField(ImageButton.class, "sprites");
    private static final Field KEY_MAPPING_PRESS_TIME = ReflectUtil.findField(KeyMapping.class, "clickCount");
    private static final Field STONE_CUTTER_INDEX = ReflectUtil.findField(StonecutterScreen.class, "startIndex");
    private static final Field LOOM_START_ROW = ReflectUtil.findField(LoomScreen.class, "startRow");
    private static final Field MOVE_VECTOR = ReflectUtil.findField(ClientInput.class, "moveVector");

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

    private static Field findField(Class<?> targetClass, String fieldName)
    {
        try
        {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }
        catch(NoSuchFieldException e)
        {
            throw new RuntimeException(e);
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
    public static WidgetSprites getImageButtonResource(ImageButton button)
    {
        try
        {
            return (WidgetSprites) IMAGE_BUTTON_SPRITES.get(button);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
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

    public static void updateMoveVector(ClientInput input, Vector2f vec)
    {
        try
        {
            MOVE_VECTOR.set(input, new Vec2(vec.x, vec.y));
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
