package com.mrcrayfish.controllable.client.gui.navigation;

import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

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
    private final int dir;
    private int itemY;

    public ListEntryNavigationPoint(AbstractSelectionList<?> list, GuiEventListener listEntry, int index, int dir)
    {
        super(0, 0, Type.BASIC);
        this.list = list;
        this.listEntry = listEntry;
        this.index = index;
        this.itemHeight = ClientServices.CLIENT.getListItemHeight(this.list);
        this.dir = dir;
        this.itemY = ClientServices.CLIENT.getAbstractListRowTop(this.list, index) + this.itemHeight / 2 - 2;
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
        return this.itemY;
    }

    @Override
    public void onNavigate()
    {
        int index = this.index;
        GuiEventListener entry = this.listEntry;
        List<? extends GuiEventListener> children = this.list.children();

        // Make the navigation skip to the next item in the list. Used to skip "title" items.
        if(entry instanceof SkipItem)
        {
            int skipIndex = index + this.dir;
            if(skipIndex >= 0 && skipIndex < children.size())
            {
                index = skipIndex;
                entry = children.get(skipIndex);
            }
        }

        // Make list scroll to top if next item is the first item and is skippable
        if(index + this.dir == 0 && children.size() > 0 && children.get(0) instanceof SkipItem)
        {
            entry = children.get(0);
        }

        int rowTop = ClientServices.CLIENT.getAbstractListRowTop(this.list, index);
        int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(this.list, index);
        int listTop = ClientServices.CLIENT.getAbstractListTop(this.list);
        int listBottom = ClientServices.CLIENT.getAbstractListBottom(this.list);
        if(rowTop < listTop + this.itemHeight / 2)
        {
            double scroll = this.list.children().indexOf(entry) * this.itemHeight - this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
        if(rowBottom > listBottom - this.itemHeight / 2) // Is not/partially visible
        {
            double scroll = this.list.children().indexOf(entry) * this.itemHeight + this.itemHeight - (listBottom - listTop) + 4 + this.itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }

        this.itemY = ClientServices.CLIENT.getAbstractListRowTop(this.list, index) + this.itemHeight / 2 - 2;
    }
}
