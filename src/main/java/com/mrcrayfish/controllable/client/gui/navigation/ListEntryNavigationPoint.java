package com.mrcrayfish.controllable.client.gui.navigation;

import com.mrcrayfish.controllable.client.util.ReflectUtil;
import net.minecraft.client.gui.components.AbstractSelectionList;

/**
 * Author: MrCrayfish
 */
public class ListEntryNavigationPoint extends NavigationPoint
{
    private final AbstractSelectionList<?> list;
    private final AbstractSelectionList.Entry<?> entry;
    private final int index;
    private final int itemHeight;

    public ListEntryNavigationPoint(AbstractSelectionList<?> list, AbstractSelectionList.Entry<?> entry, int index)
    {
        super(0, 0, Type.BASIC);
        this.list = list;
        this.entry = entry;
        this.index = index;
        this.itemHeight = ReflectUtil.getListItemHeight(this.list);
    }

    @Override
    public double distanceTo(double x, double y)
    {
        return Math.sqrt(Math.pow(this.getX() - x, 2) + Math.pow(this.getY() - y, 2));
    }

    @Override
    public double getX()
    {
        return this.list.getRowLeft() + this.list.getRowWidth() / 2;
    }

    @Override
    public double getY()
    {
        return ReflectUtil.getAbstractListRowTop(this.list, this.index) + this.itemHeight / 2 - 2;
    }

    @Override
    public void onNavigate()
    {
        int y1 = ListWidgetNavigationPoint.getListY1(this.list);
        int y0 = ListWidgetNavigationPoint.getListY0(this.list);
        int index = this.list.children().indexOf(this.entry);
        int rowTop = ReflectUtil.getAbstractListRowTop(this.list, index);
        int rowBottom = ReflectUtil.getAbstractListRowBottom(this.list, index);
        if(rowTop < this.list.getTop() + this.itemHeight / 2)
        {
            double scroll = this.list.children().indexOf(this.entry) * this.itemHeight - this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
        if(rowBottom > this.list.getBottom() - this.itemHeight / 2) // Is not/partially visible
        {
            double scroll = this.list.children().indexOf(this.entry) * this.itemHeight + this.itemHeight - (y1 - y0) + 4 + this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
    }
}
