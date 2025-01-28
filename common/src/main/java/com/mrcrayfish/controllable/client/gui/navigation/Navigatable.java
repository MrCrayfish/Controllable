package com.mrcrayfish.controllable.client.gui.navigation;

import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public interface Navigatable
{
    List<GuiEventListener> elements();
}
