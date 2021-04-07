package com.mrcrayfish.controllable.event;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GatherRadialMenuItemsEvent extends Event
{
    private List<RadialMenuHandler.AbstractRadialItem> items = new ArrayList<>();

    public void addItem(RadialMenuHandler.AbstractRadialItem item)
    {
        this.items.add(item);
    }

    public List<RadialMenuHandler.AbstractRadialItem> getItems()
    {
        return ImmutableList.copyOf(this.items);
    }
}
