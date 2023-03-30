package com.mrcrayfish.controllable.client.gui.navigation;

import com.mrcrayfish.controllable.client.util.ReflectUtil;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class ListWidgetNavigationPoint extends NavigationPoint
{
    private final AbstractWidget widget;
    private final AbstractSelectionList<?> list;
    private final AbstractSelectionList.Entry<?> entry;

    public ListWidgetNavigationPoint(AbstractWidget widget, AbstractSelectionList<?> list, AbstractSelectionList.Entry<?> entry)
    {
        super(0, 0, Type.WIDGET);
        this.widget = widget;
        this.list = list;
        this.entry = entry;
    }

    @Override
    public double distanceTo(double x, double y)
    {
        return Math.sqrt(Math.pow(this.getX() - x, 2) + Math.pow(this.getY() - y, 2));
    }

    @Override
    public double getX()
    {
        return this.widget.getX() + this.widget.getWidth() / 2;
    }

    @Override
    public double getY()
    {
        return this.widget.getY() + this.widget.getHeight() / 2;
    }

    @Override
    public void onNavigate()
    {
        int itemHeight = ReflectUtil.getListItemHeight(this.list);
        int y1 = getListY1(this.list);
        int y0 = getListY0(this.list);
        int index = this.list.children().indexOf(this.entry);
        int rowTop = ReflectUtil.getAbstractListRowTop(this.list, index);
        int rowBottom = ReflectUtil.getAbstractListRowBottom(this.list, index);
        if(rowTop < this.list.getTop())
        {
            double scroll = this.list.children().indexOf(this.entry) * itemHeight - itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
        if(rowBottom > this.list.getBottom()) // Is not/partially visible
        {
            double scroll = this.list.children().indexOf(this.entry) * itemHeight + itemHeight - (y1 - y0) + 4 + itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
    }

    public static int getListY0(AbstractSelectionList<?> list)
    {
        try
        {
            Field field = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93390_");
            field.setAccessible(true);
            return (int) field.get(list);
        }
        catch(IllegalAccessException e)
        {
            return 0;
        }
    }

    public static int getListY1(AbstractSelectionList<?> list)
    {
        try
        {
            Field field = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93391_");
            field.setAccessible(true);
            return (int) field.get(list);
        }
        catch(IllegalAccessException e)
        {
            return 0;
        }
    }
}
