package com.mrcrayfish.controllable.client.overlay;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TabNavigationOverlay implements IOverlay
{
    private TabNavigationBar navigationBar;

    @Override
    public void tick()
    {
        this.navigationBar = null;
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if(screen != null)
        {
            this.navigationBar = screen.children().stream().filter(e -> e instanceof TabNavigationBar).map(listener -> (TabNavigationBar) listener).findFirst().orElse(null);
        }
    }

    @Override
    public boolean isVisible()
    {
        return Controllable.getInput().isControllerInUse() && this.navigationBar != null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        List<? extends GuiEventListener> tabs = this.navigationBar.children();
        ScreenRectangle firstTab = tabs.get(0).getRectangle();
        ClientHelper.drawButton(graphics, firstTab.left() - 18, (firstTab.height() - 11) / 2, Buttons.LEFT_BUMPER);
        ScreenRectangle lastTab = tabs.get(tabs.size() - 1).getRectangle();
        ClientHelper.drawButton(graphics, lastTab.right() + 5, (lastTab.height() - 11) / 2, Buttons.RIGHT_BUMPER);
    }
}
