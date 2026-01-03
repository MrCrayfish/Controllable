package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientGridWithNavigationMixin;
import com.mrcrayfish.controllable.mixin.client.jei.IngredientListOverlayMixin;
import com.mrcrayfish.controllable.mixin.client.jei.PageNavigationMixin;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.IconButton;
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
                IconButton backButton = ((PageNavigationMixin) navigation).controllableGetBackButton();
                ImmutableRect2i area1 = backButton.getArea();
                points.add(new BasicNavigationPoint(area1.getX() + area1.getWidth() / 2.0, area1.getY() + area1.getHeight() / 2.0));

                IconButton nextButton = ((PageNavigationMixin) navigation).controllableGetNextButton();
                ImmutableRect2i area2 = nextButton.getArea();
                points.add(new BasicNavigationPoint(area2.getX() + area2.getWidth() / 2.0, area2.getY() + area2.getHeight() / 2.0));

                IconButton configButton = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).controllableGetConfigButton();
                ImmutableRect2i area3 = configButton.getArea();
                points.add(new BasicNavigationPoint(area3.getX() + area3.getWidth() / 2.0, area3.getY() + area3.getHeight() / 2.0));
            }
        });
        return points;
    }
}
