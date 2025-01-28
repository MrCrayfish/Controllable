package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.components.AbstractWidget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class EmiSupport
{
    private static final Field panelsField;

    static
    {
        try
        {
            panelsField = EmiScreenManager.class.getDeclaredField("panels");
            panelsField.setAccessible(true);
        }
        catch(NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean invokeMouseClick(int button, double cursorX, double cursorY)
    {
        return EmiScreenManager.mouseClicked(cursorX, cursorY, button);
    }

    public static boolean invokeMouseReleased(int button, double cursorX, double cursorY)
    {
        return EmiScreenManager.mouseReleased(cursorX, cursorY, button);
    }

    public static boolean invokeMouseDragged(int button, double cursorX, double cursorY, double deltaX, double deltaY)
    {
        return EmiScreenManager.mouseDragged(cursorX, cursorY, button, deltaX, deltaY);
    }

    public static List<NavigationPoint> getNavigationPoints()
    {
        List<NavigationPoint> points = new ArrayList<>();
        getPanels().forEach(panel ->
        {
            if(!panel.isVisible())
                return;

            addWidget(points, panel.pageLeft);
            addWidget(points, panel.pageRight);
            addWidget(points, panel.cycle);

            panel.getSpaces().forEach(space ->
            {
                int startIndex = 0;
                if(panel.space == space)
                {
                    startIndex = space.pageSize * panel.page;
                }
                List<? extends EmiIngredient> stacks = space.getStacks();
                main: for (int y = 0; y < space.th; y++)
                {
                    for(int x = 0; x < space.getWidth(y); x++)
                    {
                        if (startIndex >= stacks.size())
                        {
                            break main;
                        }
                        int slotX = space.getX(x, y) + 9;
                        int slotY = space.getY(x, y) + 9;
                        points.add(new BasicNavigationPoint(slotX, slotY));
                    }
                }
            });
        });
        return points;
    }

    private static void addWidget(List<NavigationPoint> points, AbstractWidget widget)
    {
        if(widget.visible && widget.active)
        {
            points.add(new WidgetNavigationPoint(widget));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<EmiScreenManager.SidebarPanel> getPanels()
    {
        try
        {
            return (List<EmiScreenManager.SidebarPanel>) panelsField.get(null);
        }
        catch(IllegalAccessException e)
        {
            return Collections.emptyList();
        }
    }
}
