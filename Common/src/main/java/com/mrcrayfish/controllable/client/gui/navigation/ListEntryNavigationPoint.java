package com.mrcrayfish.controllable.client.gui.navigation;

import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class ListEntryNavigationPoint extends NavigationPoint
{
    private final AbstractSelectionList<?> list;
    private final GuiEventListener listEntry;
    private final int index;
    private final int itemHeight;

    public ListEntryNavigationPoint(AbstractSelectionList<?> list, GuiEventListener listEntry, int index)
    {
        super(0, 0, Type.BASIC);
        this.list = list;
        this.listEntry = listEntry;
        this.index = index;
        this.itemHeight = ClientServices.CLIENT.getListItemHeight(this.list);
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
        return ClientServices.CLIENT.getAbstractListRowTop(this.list, this.index) + this.itemHeight / 2 - 2;
    }

    @Override
    public void onNavigate()
    {
        int index = this.list.children().indexOf(this.listEntry);
        int rowTop = ClientServices.CLIENT.getAbstractListRowTop(this.list, index);
        int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(this.list, index);
        int listTop = ClientServices.CLIENT.getAbstractListTop(this.list);
        int listBottom = ClientServices.CLIENT.getAbstractListBottom(this.list);
        if(rowTop < listTop + this.itemHeight / 2)
        {
            double scroll = this.list.children().indexOf(this.listEntry) * this.itemHeight - this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
        if(rowBottom > listBottom - this.itemHeight / 2) // Is not/partially visible
        {
            double scroll = this.list.children().indexOf(this.listEntry) * this.itemHeight + this.itemHeight - (listBottom - listTop) + 4 + this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
    }
}
