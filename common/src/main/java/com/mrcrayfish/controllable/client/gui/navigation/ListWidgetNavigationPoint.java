package com.mrcrayfish.controllable.client.gui.navigation;

import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class ListWidgetNavigationPoint extends NavigationPoint
{
    private final AbstractWidget widget;
    private final AbstractSelectionList<?> list;
    private final GuiEventListener listEntry;

    public ListWidgetNavigationPoint(AbstractWidget widget, AbstractSelectionList<?> list, GuiEventListener listEntry)
    {
        super(0, 0, Type.WIDGET);
        this.widget = widget;
        this.list = list;
        this.listEntry = listEntry;
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
        int itemHeight = ClientServices.CLIENT.getListItemHeight(this.list);
        int index = this.list.children().indexOf(this.listEntry);
        int rowTop = ClientServices.CLIENT.getAbstractListRowTop(this.list, index);
        int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(this.list, index);
        int listTop = ClientServices.CLIENT.getAbstractListTop(this.list);
        int listBottom = ClientServices.CLIENT.getAbstractListBottom(this.list);
        if(rowTop < listTop)
        {
            double scroll = this.list.children().indexOf(this.listEntry) * itemHeight - itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
        if(rowBottom > listBottom) // Is not/partially visible
        {
            double scroll = this.list.children().indexOf(this.listEntry) * itemHeight + itemHeight - (listBottom - listTop) + 4 + itemHeight / 2;
            this.list.setScrollAmount(scroll);
        }
    }

    @Override
    public boolean shouldHide()
    {
        return this.widget instanceof AbstractButton || this.widget instanceof TabButton || this.widget instanceof HideCursor;
    }
}
