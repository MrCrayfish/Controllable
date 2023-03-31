package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use {@link ControllerEvents#GATHER_NAVIGATION_POINTS} instead
 */
@Deprecated
public class GatherNavigationPointsEvent extends Event
{
    private final List<NavigationPoint> points = new ArrayList<>();

    public void addPoint(NavigationPoint point)
    {
        this.points.add(point);
    }

    public List<NavigationPoint> getPoints()
    {
        return Collections.unmodifiableList(this.points);
    }
}
