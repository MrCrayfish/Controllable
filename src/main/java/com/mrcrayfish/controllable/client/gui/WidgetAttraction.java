package com.mrcrayfish.controllable.client.gui;

import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import org.codehaus.plexus.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WidgetAttraction
{
    private static List<Class> classWhitelist = new ArrayList<Class>() {{
        add(RecipeBookGui.class);
        add(RecipeBookPage.class);
    }};

    public static List<Widget> getWidgets(Screen screen)
    {
        List<Widget> widgets = new ArrayList<>(getWidgets(screen, 0));
        widgets.removeIf(Objects::isNull); // Remove nulls
        widgets = widgets.stream().distinct().collect(Collectors.toList()); // Remove possible duplicates
        return widgets;
    }

    private static List<Widget> getWidgets(Object parent, int level)
    {
        List<Widget> widgets = new ArrayList<>();

        ReflectionUtils.getFieldsIncludingSuperclasses(parent.getClass()).forEach(f -> {
            try
            {
                if(Widget.class.isAssignableFrom(f.getType()))
                {
                    f.setAccessible(true);
                    widgets.add((Widget) f.get(parent));
                }
                else if(List.class.isAssignableFrom(f.getType()))
                {
                    f.setAccessible(true);
                    List l = (List) f.get(parent);
                    if (l != null && !l.isEmpty())
                    {
                        for(Object o : l)
                        {
                            if(!(o instanceof Widget))
                            {
                                break;
                            }
                            Widget w = (Widget) o;
                            if(w.visible)
                            {
                                widgets.add(w);
                            }
                        }
                    }
                }
                else if(classWhitelist.contains(f.getType()))
                {
                    f.setAccessible(true);
                    widgets.addAll(getWidgets(f.get(parent), level + 1));
                }
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });

        return widgets;
    }
}
