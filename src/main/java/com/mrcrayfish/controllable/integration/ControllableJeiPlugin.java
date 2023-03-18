package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.mixin.client.jei.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@JeiPlugin
public class ControllableJeiPlugin implements IModPlugin
{
    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "jei_plugin");

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime)
    {
        ControllableJeiPlugin.runtime = runtime;
    }

    @Override
    public void onRuntimeUnavailable()
    {
        ControllableJeiPlugin.runtime = null;
    }

    public static List<NavigationPoint> getNavigationPoints()
    {
        List<NavigationPoint> points = new ArrayList<>();
        Optional.ofNullable(ControllableJeiPlugin.runtime).ifPresent(runtime ->
        {
            if(runtime.getIngredientListOverlay().isListDisplayed())
            {
                // JEI just needs getters, and I wouldn't have to do this mess
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
                points.add(new WidgetNavigationPoint(backButton.getX() + backButton.getWidth() / 2.0, backButton.getY() + backButton.getHeight() / 2.0, backButton));
                GuiIconButton nextButton = ((PageNavigationMixin) navigation).getNextButton();
                points.add(new WidgetNavigationPoint(nextButton.getX() + nextButton.getWidth() / 2.0, nextButton.getY() + nextButton.getHeight() / 2.0, nextButton));

                GuiIconToggleButton configToggleButton = ((IngredientListOverlayMixin) runtime.getIngredientListOverlay()).getConfigButton();
                GuiIconButton configButton = ((GuiIconToggleButtonMixin) configToggleButton).getButton();
                points.add(new WidgetNavigationPoint(configButton.getX() + configButton.getWidth() / 2.0, configButton.getY() + configButton.getHeight() / 2.0, nextButton));
            }
        });
        return points;
    }
}