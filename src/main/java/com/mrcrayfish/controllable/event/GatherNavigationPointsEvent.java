package com.mrcrayfish.controllable.event;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GatherNavigationPointsEvent extends Event
{
    private List<NavigationPoint> points = new ArrayList<>();

    public void addPoint(NavigationPoint point)
    {
        this.points.add(point);
    }

    public List<NavigationPoint> getPoints()
    {
        return ImmutableList.copyOf(this.points);
    }
}
