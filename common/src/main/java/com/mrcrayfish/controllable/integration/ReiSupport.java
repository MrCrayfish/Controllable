package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("UnstableApiUsage")
public class ReiSupport
{
    //private static final Field tabsField = getField(AbstractDisplayViewingScreen.class, "tabs");

    public static List<NavigationPoint> getNavigationPoints(Screen screen)
    {
        /*if(!REIRuntime.getInstance().isOverlayVisible())
            return Collections.emptyList();

        List<NavigationPoint> points = new ArrayList<>();
        REIRuntime.getInstance().getOverlay().ifPresent(overlay -> {
            if(overlay.getBounds().isEmpty())
                return;
            scanWidgets(points, overlay);
            scanWidgets(points, overlay.getEntryList());
        });

        if(screen instanceof AbstractDisplayViewingScreen s1)
        {
            scanWidgets(points, getTabs(s1));
            if(s1 instanceof DefaultDisplayViewingScreen s2)
            {
                for(Widget widget : s2.widgets())
                {
                    scanWidgets(points, widget);
                }
            }
        }

        return points;*/
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static void scanWidgets(List<NavigationPoint> points, Object obj)
    {
        /*if(obj instanceof ContainerEventHandler handler)
        {
            for(GuiEventListener listener : handler.children())
            {
                scanWidgets(points, listener);
            }
        }
        if(obj instanceof TabContainerWidget container)
        {
            for(Widget widget : container.widgets())
            {
                scanWidgets(points, widget);
            }
        }
        else if(obj instanceof WidgetWithBounds widget && isValidWidgetType(widget))
        {
            Rectangle box = widget.getBounds();
            points.add(new BasicNavigationPoint(box.x + box.width / 2.0, box.y + box.height / 2.0));
        }*/
    }

    private static boolean isValidWidgetType(Object obj)
    {
        /*if(obj instanceof Button)
            return true;
        if(obj instanceof EntryListStackEntry)
            return true;
        if(obj instanceof Label label && label.isClickable())
            return true;
        if(obj instanceof TabWidget tabWidget && tabWidget.isShown() && !tabWidget.bounds.isEmpty() && tabWidget.opacity > 0)
            return true;
        if(obj instanceof Slot slot && slot.isInteractable())
            return true;*/
        return false;
    }

    /*private static TabContainerWidget getTabs(AbstractDisplayViewingScreen screen)
    {
        *//*try
        {
            return (TabContainerWidget) tabsField.get(screen);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }*//*
        return null;
    }*/

    private static Field getField(Class<?> targetClass, String fieldName)
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
}