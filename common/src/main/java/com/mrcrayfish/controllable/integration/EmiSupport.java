package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import dev.emi.emi.widget.RecipeBackground;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class EmiSupport
{
    private static final Field widgetGroupsField = getField(RecipeScreen.class, "currentPage");
    private static final Field panelsField = getField(EmiScreenManager.class, "panels");
    private static final Field recipeTabsField = getField(RecipeScreen.class, "tabs");
    private static final Field tabField = getField(RecipeScreen.class, "tab");
    private static final Field tabCountField = getField(RecipeScreen.class, "tabPageSize");
    private static final Field tabPageField = getField(RecipeScreen.class, "tabPage");
    private static final Field tabOffsetField = getField(RecipeScreen.class, "tabOff");
    private static final Field xField = getField(RecipeScreen.class, "x");
    private static final Field yField = getField(RecipeScreen.class, "y");

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

    public static List<NavigationPoint> getNavigationPoints(Screen screen)
    {
        List<NavigationPoint> points = new ArrayList<>();

        addWidget(points, EmiScreenManager.search);
        addWidget(points, EmiScreenManager.emi);
        addWidget(points, EmiScreenManager.tree);

        for(EmiScreenManager.SidebarPanel panel : getPanels())
        {
            if(!panel.isVisible())
                continue;

            addWidget(points, panel.pageLeft);
            addWidget(points, panel.pageRight);
            addWidget(points, panel.cycle);

            for(EmiScreenManager.ScreenSpace space : panel.getSpaces())
            {
                int startIndex = (panel.space == space) ? space.pageSize * panel.page : 0;
                List<? extends EmiIngredient> stacks = space.getStacks();
                main: for (int y = 0; y < space.th; y++)
                {
                    for(int x = 0; x < space.getWidth(y); x++)
                    {
                        if (startIndex >= stacks.size())
                            break main;
                        int slotX = space.getX(x, y) + 9;
                        int slotY = space.getY(x, y) + 9;
                        points.add(new BasicNavigationPoint(slotX, slotY));
                    }
                }
            }
        }

        if(screen instanceof RecipeScreen recipeScreen)
        {
            // Add the recipe tabs as a possible navigation point
            List<RecipeTab> tabs = getRecipeTabs(recipeScreen);
            int tab = getInt(recipeScreen, tabField);
            int page = getInt(recipeScreen, tabPageField);
            int count = getInt(recipeScreen, tabCountField);
            int x = getInt(recipeScreen, xField);
            int y = getInt(recipeScreen, yField);
            int offset = getInt(recipeScreen, tabOffsetField);
            for(RecipeTab recipeTab : tabs)
            {
                int nextOffset = 0;
                int startIndex = page * count;
                for(int i = startIndex; i < tabs.size() && i < startIndex + count; i++, nextOffset++)
                {
                    int tabX = x + offset + nextOffset * 24 + 16 + 24 / 2;
                    int tabY = y - 24 + 27 / 2;
                    points.add(new BasicNavigationPoint(tabX, tabY));
                }
            }

            // Add the workstations at the bottom
            RecipeTab recipeTab = tabs.get(tab);
            int size = EmiApi.getRecipeManager().getWorkstations(recipeTab.category).size();
            if(size > 0 || RecipeScreen.resolve != null)
            {
                Bounds box = recipeScreen.getWorkstationBounds(-1);
                points.add(new BasicNavigationPoint(box.x() + box.width() / 2.0, box.y() + box.height() / 2.0));
            }

            // Add slots from current page
            for(WidgetGroup group : getWidgetGroups(recipeScreen))
            {
                for(Widget widget : group.widgets)
                {
                    if(!isValidWidget(widget))
                        continue;

                    Bounds box = widget.getBounds();
                    int widgetX = group.x + (int) (box.x() + box.width() / 2.0);
                    int widgetY = group.y + (int) (box.y() + box.height() / 2.0);
                    points.add(new BasicNavigationPoint(widgetX, widgetY));
                }
            }
        }

        return points;
    }

    private static boolean isValidWidget(Widget widget)
    {
        if(widget instanceof DrawableWidget)
            return false;
        if(widget instanceof TextureWidget)
            return false;
        if(widget instanceof RecipeBackground)
            return false;
        return true;
    }

    private static void addWidget(List<NavigationPoint> points, AbstractWidget widget)
    {
        if(widget.visible && widget.active)
        {
            points.add(new WidgetNavigationPoint(widget));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<EmiScreenManager.SidebarPanel> getPanels()
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

    @SuppressWarnings("unchecked")
    private static List<RecipeTab> getRecipeTabs(RecipeScreen screen)
    {
        try
        {
            return (List<RecipeTab>) recipeTabsField.get(screen);
        }
        catch(IllegalAccessException e)
        {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<WidgetGroup> getWidgetGroups(RecipeScreen screen)
    {
        try
        {
            return (List<WidgetGroup>) widgetGroupsField.get(screen);
        }
        catch(IllegalAccessException e)
        {
            return Collections.emptyList();
        }
    }

    private static int getInt(Object instance, Field field)
    {
        try
        {
            return (int) field.get(instance);
        }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

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