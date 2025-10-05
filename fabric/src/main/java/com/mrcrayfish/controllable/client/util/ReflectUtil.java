package com.mrcrayfish.controllable.client.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class ReflectUtil
{
    private static final Method ABSTRACT_CONTAINER_SCREEN_CLICK_SLOT = ReflectUtil.findMethod(AbstractContainerScreen.class, "net.minecraft.class_465", "method_2383", "(Lnet/minecraft/class_1735;IILnet/minecraft/class_1713;)V", Slot.class, int.class, int.class, ClickType.class);
    private static final Field ABSTRACT_SELECTION_LIST_ITEM_HEIGHT = ReflectUtil.findField(AbstractSelectionList.class, "net.minecraft.class_350", "field_62109", "I");
    private static final Field IMAGE_BUTTON_SPRITES = ReflectUtil.findField(ImageButton.class, "net.minecraft.class_344", "field_45356", "Lnet/minecraft/class_8666;");
    private static final Field CREATIVE_SCREEN_SCROLL_OFFSET = ReflectUtil.findField(CreativeModeInventoryScreen.class, "net.minecraft.class_481", "field_2890", "F");
    private static final Field KEY_MAPPING_PRESS_TIME = ReflectUtil.findField(KeyMapping.class, "net.minecraft.class_304", "field_1661", "I");
    private static final Field STONE_CUTTER_INDEX = ReflectUtil.findField(StonecutterScreen.class, "net.minecraft.class_3979", "field_17671", "I");
    private static final Field LOOM_START_ROW = ReflectUtil.findField(LoomScreen.class, "net.minecraft.class_494", "field_39190", "I");
    private static final Field MOVE_VECTOR = ReflectUtil.findField(ClientInput.class, "net.minecraft.class_744", "field_55868", "Lnet/minecraft/class_241;");

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
