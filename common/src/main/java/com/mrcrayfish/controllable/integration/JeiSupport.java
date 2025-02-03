package com.mrcrayfish.controllable.integration;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.mixin.client.jei.*;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListRenderer;
import mezz.jei.gui.recipes.RecipeCatalysts;
import mezz.jei.gui.recipes.RecipeGuiTab;
import mezz.jei.gui.recipes.RecipeGuiTabs;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class JeiSupport
{
    @SuppressWarnings("removal")
    public static List<NavigationPoint> getNavigationPoints()
    {
        List<NavigationPoint> points = new ArrayList<>();
        Optional.ofNullable(ControllableJeiPlugin.getRuntime()).ifPresent(runtime ->
        {
            if(runtime.getIngredientListOverlay().isListDisplayed())
            {
                // Add each item on the ingredient grid
                getIngredientListRenderer(runtime).getSlots().forEach(slot -> {
                    ImmutableRect2i area = slot.getArea();
                    points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
                });

                // Add the navigation buttons shown at the top of the ingredient grid
                getGridPageNavigation(runtime).ifPresent(nav -> {
                    addWidget(points, ((PageNavigationAccessor) nav).controllableGetBackButton());
                    addWidget(points, ((PageNavigationAccessor) nav).controllableGetNextButton());
                });

                // Add the config button found at the bottom right of the ingredient grid
                GuiIconToggleButton configToggleButton = ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).controllableGetConfigButton();
                GuiIconButton configButton = ((GuiIconToggleButtonAccessor) configToggleButton).controllableGetButton();
                points.add(new WidgetNavigationPoint(configButton));

                // Add the recipe slots on the left side of the recipes gui
                getRecipeSlots(runtime).forEach(drawable -> {
                    Rect2i area = drawable.getRect();
                    points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
                });

                // Add the recipe tabs at the top of recipe gui
                getRecipeTabs(runtime).forEach(tab -> {
                    ImmutableRect2i area = ((RecipeGuiTabAccessor) tab).controllableGetArea();
                    points.add(new BasicNavigationPoint(area.getX() + area.getWidth() / 2.0, area.getY() + area.getHeight() / 2.0));
                });

                // Add the navigation buttons at the top of the recipe tabs
                getRecipeTabPageNavigation(runtime).ifPresent(nav -> {
                    addWidget(points, ((PageNavigationAccessor) nav).controllableGetBackButton());
                    addWidget(points, ((PageNavigationAccessor) nav).controllableGetNextButton());
                });

                // Add the slots added by layouts
                getRecipeLayouts(runtime).forEach(layout -> {

                    Rect2i pos = layout.getRect();
                    int layoutX = pos.getX();
                    int layoutY = pos.getY();
                    for(IRecipeSlotDrawable drawable : layout.getRecipeSlots().getSlots())
                    {
                        Rect2i area = drawable.getRect();
                        points.add(new BasicNavigationPoint(layoutX + area.getX() + area.getWidth() / 2.0, layoutY + area.getY() + area.getHeight() / 2.0));
                    }
                });

                // Transfer buttons
                getTransferButtons(runtime).forEach(button -> {
                    addWidget(points, button);
                });
            }
        });
        return points;
    }

    private static void addWidget(List<NavigationPoint> points, AbstractWidget widget)
    {
        if(widget.visible && widget.active)
        {
            points.add(new WidgetNavigationPoint(widget));
        }
    }

    private static IngredientListRenderer getIngredientListRenderer(IJeiRuntime runtime)
    {
        IngredientGridWithNavigation a = ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).controllableGetContents();
        IngredientGrid b = ((IngredientGridWithNavigationAccessor) a).controllableGetIngredientGrid();
        return ((IngredientGridAccessor) b).controllableGetIngredientListRenderer();
    }

    private static Optional<PageNavigation> getGridPageNavigation(IJeiRuntime runtime)
    {
        IngredientGridWithNavigation grid = ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).controllableGetContents();
        PageNavigation navigation = ((IngredientGridWithNavigationAccessor) grid).controllableGetNavigation();
        if(((PageNavigationAccessor) navigation).controllableIsVisible())
        {
            return Optional.of(navigation);
        }
        return Optional.empty();
    }

    private static List<IRecipeSlotDrawable> getRecipeSlots(IJeiRuntime runtime)
    {
        if(runtime.getRecipesGui() instanceof RecipesGui gui)
        {
            RecipeCatalysts catalysts = ((RecipesGuiAccessor) gui).controllableGetCatalysts();
            return ((RecipeCatalystsAccessor) catalysts).controllableGetRecipeSlots();
        }
        return Collections.emptyList();
    }

    private static List<RecipeGuiTab> getRecipeTabs(IJeiRuntime runtime)
    {
        if(runtime.getRecipesGui() instanceof RecipesGui gui)
        {
            RecipeGuiTabs tabs = ((RecipesGuiAccessor) gui).controllableGetRecipeGuiTabs();
            return ((RecipeGuiTabsAccessor) tabs).controllableGetTabs();
        }
        return Collections.emptyList();
    }

    private static Optional<PageNavigation> getRecipeTabPageNavigation(IJeiRuntime runtime)
    {
        if(runtime.getRecipesGui() instanceof RecipesGui gui)
        {
            RecipeGuiTabs tabs = ((RecipesGuiAccessor) gui).controllableGetRecipeGuiTabs();
            PageNavigation navigation = ((RecipeGuiTabsAccessor) tabs).controllableGetNavigation();
            if(((PageNavigationAccessor) navigation).controllableIsVisible())
            {
                return Optional.of(navigation);
            }
        }
        return Optional.empty();
    }

    private static Stream<RecipeLayout<?>> getRecipeLayouts(IJeiRuntime runtime)
    {
        if(runtime.getRecipesGui() instanceof RecipesGui gui)
        {
            return ((RecipesGuiAccessor) gui).controllableGetLayouts().stream()
                .filter(drawable -> {
                    return drawable instanceof RecipeLayout<?>;
                }).map(drawable -> {
                    return (RecipeLayout<?>) drawable;
                });
        }
        return Stream.empty();
    }

    private static List<RecipeTransferButton> getTransferButtons(IJeiRuntime runtime)
    {
        if(runtime.getRecipesGui() instanceof RecipesGui gui)
        {
            return ((RecipesGuiAccessor) gui).getTransferButtons();
        }
        return Collections.emptyList();
    }

}
