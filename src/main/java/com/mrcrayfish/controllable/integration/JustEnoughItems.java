package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridWithNavigationMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientListOverlayMixin;
import com.mrcrayfish.controllable.mixin.client.jei.PageNavigationMixin;
import mezz.jei.Internal;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.render.IngredientListBatchRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class JustEnoughItems
{
    public static List<NavigationPoint> getNavigationPoints()
    {
        List<NavigationPoint> points = new ArrayList<>();
        JeiRuntime runtime = Internal.getRuntime();
        if(runtime != null && runtime.getIngredientListOverlay().isListDisplayed())
        {
            // JEI just needs getters and I wouldn't have to do this mess
            IngredientGridWithNavigation ingredientGridWithNavigation = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).getContents();
            IngredientGrid ingredientGrid = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).getIngredientGrid();
            IngredientListBatchRenderer ingredientListBatchRenderer = ((IngredientGridMixin) ingredientGrid).getGuiIngredientSlots();

            List<IngredientListSlot> slots = ingredientListBatchRenderer.getAllGuiIngredientSlots();
            for(IngredientListSlot slot : slots)
            {
                Rectangle2d area = slot.getArea();
                points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
            }

            PageNavigation navigation = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).getNavigation();
            Widget backButton = ((PageNavigationMixin) navigation).getBackButton();
            points.add(new WidgetNavigationPoint(backButton.x + backButton.getWidth() / 2.0, backButton.y + backButton.getHeight() / 2.0, backButton));
            Widget nextButton = ((PageNavigationMixin) navigation).getNextButton();
            points.add(new WidgetNavigationPoint(nextButton.x + nextButton.getWidth() / 2.0, nextButton.y + nextButton.getHeight() / 2.0, nextButton));
        }
        return points;
    }
}
