package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.inventory.Slot;

/**
 * Author: MrCrayfish
 */
public class SlotNavigationPoint extends NavigationPoint
{
    private Slot slot;

    public SlotNavigationPoint(double x, double y, Slot slot)
    {
        super(x, y, Type.SLOT);
        this.slot = slot;
    }

    public Slot getSlot()
    {
        return this.slot;
    }
}
