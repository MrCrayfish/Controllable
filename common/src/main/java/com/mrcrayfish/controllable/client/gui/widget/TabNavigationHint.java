package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TabNavigationHint implements Renderable
{
    private final List<? extends GuiEventListener> tabs;

    public TabNavigationHint(List<? extends GuiEventListener> tabs)
    {
        this.tabs = tabs;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // Don't render if tabs can't be navigated
        if(!Controllable.getInput().isControllerInUse())
            return;

        ScreenRectangle firstTab = this.tabs.get(0).getRectangle();
        ClientHelper.drawButton(graphics, firstTab.left() - 18, (firstTab.height() - 11) / 2, Buttons.LEFT_BUMPER);

        ScreenRectangle lastTab = this.tabs.get(this.tabs.size() - 1).getRectangle();
        ClientHelper.drawButton(graphics, lastTab.right() + 5, (lastTab.height() - 11) / 2, Buttons.RIGHT_BUMPER);
    }
}
