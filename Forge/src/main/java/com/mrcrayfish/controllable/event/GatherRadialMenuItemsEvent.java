package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use {@link ControllerEvents#GATHER_RADIAL_MENU_ITEMS} instead
 */
@Deprecated
public class GatherRadialMenuItemsEvent extends Event
{
    private final List<RadialMenuHandler.AbstractRadialItem> items = new ArrayList<>();

    public void addItem(RadialMenuHandler.AbstractRadialItem item)
    {
        this.items.add(item);
    }

    public List<RadialMenuHandler.AbstractRadialItem> getItems()
    {
        return Collections.unmodifiableList(this.items);
    }
}
