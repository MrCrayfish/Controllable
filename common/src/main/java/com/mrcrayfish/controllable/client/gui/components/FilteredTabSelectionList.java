package com.mrcrayfish.controllable.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.LinkedList;
import java.util.List;

public class FilteredTabSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends TabSelectionList<E>
{
    private final List<E> original = new LinkedList<>();
    private boolean preventEntries = false;

    public FilteredTabSelectionList(Minecraft mc, int itemHeight)
    {
        super(mc, itemHeight);
    }

    public void rebuildList(boolean scroll)
    {
        List<E> newEntries = this.original.stream().filter(e -> {
            if(e instanceof FilteredItem item) {
                return item.isVisible();
            }
            return true;
        }).toList();
        this.replaceEntries(newEntries);
        if(scroll)
        {
            this.setScrollAmount(this.maxScrollAmount());
        }
    }

    @Override
    public int addEntry(E entry)
    {
        if(!this.preventEntries) this.original.add(entry);
        return super.addEntry(entry);
    }

    @Override
    protected void addEntryToTop(E entry)
    {
        if(!this.preventEntries) this.original.addFirst(entry);
        super.addEntryToTop(entry);
    }

    public void finalise()
    {
        this.preventEntries = true;
    }
}
