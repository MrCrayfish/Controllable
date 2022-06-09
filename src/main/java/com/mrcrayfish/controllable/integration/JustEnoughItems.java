package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;

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
        /*JeiRuntime runtime = Internal.getRuntime();
        if(runtime != null && runtime.getIngredientListOverlay().isListDisplayed())
        {
            // JEI just needs getters and I wouldn't have to do this mess
            IngredientGridWithNavigation ingredientGridWithNavigation = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).getContents();
            IngredientGrid ingredientGrid = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).getIngredientGrid();
            IngredientListRenderer ingredientListRenderer = ((IngredientGridMixin) ingredientGrid).getIngredientListRenderer();

            // Add each item on the screen as a navigation point
            ingredientListRenderer.getSlots().forEach(slot ->
            {
                ImmutableRect2i area = slot.getArea();
                points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
            });

            PageNavigation navigation = ((IngredientGridWithNavigationMixin) ingredientGridWithNavigation).getNavigation();
            GuiIconButton backButton = ((PageNavigationMixin) navigation).getBackButton();
            points.add(new WidgetNavigationPoint(backButton.x + backButton.getWidth() / 2.0, backButton.y + backButton.getHeight() / 2.0, backButton));
            GuiIconButton nextButton = ((PageNavigationMixin) navigation).getNextButton();
            points.add(new WidgetNavigationPoint(nextButton.x + nextButton.getWidth() / 2.0, nextButton.y + nextButton.getHeight() / 2.0, nextButton));

            GuiIconToggleButton configToggleButton = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).getConfigButton();
            GuiIconButton configButton = ((GuiIconToggleButtonMixin) configToggleButton).getButton();
            points.add(new WidgetNavigationPoint(configButton.x + configButton.getWidth() / 2.0, configButton.y + configButton.getHeight() / 2.0, nextButton));
        }*/
        return points;
    }
}
