package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.mixin.client.jei.GuiIconToggleButtonMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridWithNavigationMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientListOverlayMixin;
import com.mrcrayfish.controllable.mixin.client.jei.PageNavigationMixin;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class JeiSupport
{
    public static List<NavigationPoint> getNavigationPoints()
    {
        List<NavigationPoint> points = new ArrayList<>();
        Optional.ofNullable(ControllableJeiPlugin.getRuntime()).ifPresent(runtime ->
        {
            if(runtime.getIngredientListOverlay().isListDisplayed())
            {
                // JEI just needs getters, and I wouldn't have to do this mess
                IngredientGridWithNavigation ingredientGridWithNavigation = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).controllableGetContents();
                IngredientGrid ingredientGrid = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).controllableGetIngredientGrid();
                IngredientListRenderer ingredientListRenderer = ((IngredientGridMixin) ingredientGrid).controllableGetIngredientListRenderer();

                // Add each item on the screen as a navigation point
                ingredientListRenderer.getSlots().forEach(slot ->
                {
                    ImmutableRect2i area = slot.getArea();
                    points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
                });

                PageNavigation navigation = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).controllableGetNavigation();
                GuiIconButton backButton = ((PageNavigationMixin) navigation).controllableGetBackButton();
                points.add(new WidgetNavigationPoint(backButton.getX() + backButton.getWidth() / 2.0, backButton.getY() + backButton.getHeight() / 2.0, backButton));
                GuiIconButton nextButton = ((PageNavigationMixin) navigation).controllableGetNextButton();
                points.add(new WidgetNavigationPoint(nextButton.getX() + nextButton.getWidth() / 2.0, nextButton.getY() + nextButton.getHeight() / 2.0, nextButton));

                GuiIconToggleButton configToggleButton = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).controllableGetConfigButton();
                GuiIconButton configButton = ((GuiIconToggleButtonMixin) configToggleButton).controllableGetButton();
                points.add(new WidgetNavigationPoint(configButton.getX() + configButton.getWidth() / 2.0, configButton.getY() + configButton.getHeight() / 2.0, nextButton));
            }
        });
        return points;
    }
}
