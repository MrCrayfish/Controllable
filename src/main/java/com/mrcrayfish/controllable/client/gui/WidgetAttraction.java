package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.Widget;
import org.codehaus.plexus.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WidgetAttraction
{
    private List<Widget> widgetList = new ArrayList<>();
    private final ControllerInput input;
    private Screen prevScreen;

    private List<Class> classWhitelist = new ArrayList<Class>() {{
        add(RecipeBookGui.class);
        add(RecipeBookPage.class);
    }};

    public WidgetAttraction(ControllerInput input)
    {
        this.input = input;
    }

    private void getWidgets(Screen screen)
    {
        widgetList.addAll(getWidgets(screen, 0));
        widgetList.removeIf(Objects::isNull);
        widgetList = widgetList.stream().distinct().collect(Collectors.toList());
    }

    private List<Widget> getWidgets(Object parent, int level)
    {
        List<Widget> widgets = new ArrayList<>();

        ReflectionUtils.getFieldsIncludingSuperclasses(parent.getClass()).forEach(f -> {
            try
            {
                boolean isAccessible = f.isAccessible();
                f.setAccessible(true);
                if(Widget.class.isAssignableFrom(f.getType()))
                {

                    widgets.add((Widget) f.get(parent));
                }
                else if(List.class.isAssignableFrom(f.getType()))
                {
                    List l = (List) f.get(parent);
                    for(Object o : l)
                    {
                        if(!(o instanceof Widget))
                        {
                            break;
                        }
                        Widget w = (Widget) o;
                        if (w.visible)
                        {
                            widgets.add(w);
                        }
                    }
                }
                else if(classWhitelist.contains(f.getType()))
                {
                    widgets.addAll(getWidgets(f.get(parent), level + 1));
                }
                f.setAccessible(isAccessible);
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });

        return widgets;
    }

    public void moveMouseToClosestWidget(boolean moving, Screen screen)
    {
        Minecraft mc = Minecraft.getInstance();

        if (screen != prevScreen)
        {
            widgetList.clear();
        }
        if(widgetList.isEmpty() && screen != prevScreen)
        {
            getWidgets(screen);
            prevScreen = screen;
            if(widgetList.isEmpty())
            {
                return;
            }
        }

        if(!input.moved) return;

        int mouseX = (int) (input.targetMouseX * (double) mc.mainWindow.getScaledWidth() / (double) mc.mainWindow.getWidth());
        int mouseY = (int) (input.targetMouseY * (double) mc.mainWindow.getScaledHeight() / (double) mc.mainWindow.getHeight());

        Widget closestWidget = null;
        for(Widget widget : widgetList)
        {
            if (widget == null)
            {
                continue;
            }

            if(widget.isHovered() && widget.visible)
            {
                closestWidget = widget;
            }
        }

        if(closestWidget != null)
        {
            int targetX = closestWidget.x + closestWidget.getWidth() / 2;
            if (closestWidget instanceof AbstractSlider)
            {
                targetX = mouseX;
            }
            int targetY = closestWidget.y + closestWidget.getHeight() / 2;
            input.moveMouse(mc, moving, targetX, targetY, mouseX, mouseY);
        }
        else
        {
            input.mouseSpeedX *= 0.1F;
            input.mouseSpeedY *= 0.1F;
        }
    }
}
