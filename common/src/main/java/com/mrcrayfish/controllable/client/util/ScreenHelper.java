package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ScreenHelper
{
    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static Button button(int x, int y, int width, int height, Component label, Button.OnPress onPress)
    {
        return Button.builder(label, onPress).pos(x, y).size(width, height).build();
    }

    public static void drawOutlinedBox(GuiGraphics graphics, int x, int y, int width, int height, int color)
    {
        graphics.fill(x, y, x + width, y + 1, color);                          // Top
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);                 // Left
        graphics.fill(x, y + height - 1, x + width, y + height, color);        // Bottom
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color); // Right
    }

    public static void drawRoundedBox(GuiGraphics graphics, int x, int y, int width, int height, int backgroundColor)
    {
        graphics.fill(x - 3 + 1, y, x + width + 2 - 1, y + 1, backgroundColor);
        graphics.fill(x - 3, y + 1, x + width + 2, y + height - 1, backgroundColor);
        graphics.fill(x - 3 + 1, y + height - 1, x + width + 2 - 1, y + height, backgroundColor);
    }

    public static Optional<GuiEventListener> findHoveredListener(Screen screen, double mouseX, double mouseY, Predicate<GuiEventListener> condition)
    {
        return findHoveredListener(screen.children(), mouseX, mouseY, condition);
    }

    private static Optional<GuiEventListener> findHoveredListener(List<? extends GuiEventListener> listeners, double mouseX, double mouseY, Predicate<GuiEventListener> condition)
    {
        for(GuiEventListener listener : listeners)
        {
            if(condition.test(listener) && listener.isMouseOver(mouseX, mouseY))
            {
                return Optional.of(listener);
            }
            else if(listener instanceof TabButton button && button.isSelected())
            {
                List<AbstractWidget> children = new ArrayList<>();
                button.tab().visitChildren(children::add);
                return findHoveredListener(children, mouseX, mouseY, condition);
            }
            else if(listener instanceof ContainerEventHandler handler)
            {
                return findHoveredListener(handler.children(), mouseX, mouseY, condition);
            }
        }
        return Optional.empty();
    }

    public static Optional<GuiEventListener> findHoveredEventListenerExcludeList(ContainerEventHandler handler, double mouseX, double mouseY)
    {
        Optional<GuiEventListener> hovered = findHoveredEventListenerExcludeList(handler.children(), mouseX, mouseY);
        if(hovered.isPresent())
            return hovered;
        return findHoveredEventListenerInRecipeBook(handler, mouseX, mouseY);
    }

    public static Optional<GuiEventListener> findHoveredEventListenerExcludeList(List<? extends GuiEventListener> listeners, double mouseX, double mouseY)
    {
        for(GuiEventListener listener : listeners)
        {
            if(!listener.isMouseOver(mouseX, mouseY))
                continue;

            switch(listener)
            {
                case AbstractSelectionList<?> list -> {
                    return findHoveredEventListenerFromListEntries(list, mouseX, mouseY);
                }
                case TabButton button when button.isSelected() -> {
                    List<AbstractWidget> children = new ArrayList<>();
                    button.tab().visitChildren(children::add);
                    return findHoveredEventListenerExcludeList(children, mouseX, mouseY);
                }
                case ContainerEventHandler handler -> {
                    return findHoveredEventListenerExcludeList(handler.children(), mouseX, mouseY);
                }
                default -> {
                    return Optional.of(listener);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<GuiEventListener> findHoveredEventListenerFromListEntries(AbstractSelectionList<?> list, double mouseX, double mouseY)
    {
        List<?> entries = list.children();
        for(int index = 0; index < entries.size(); index++)
        {
            // Only consider rows that are completely visible
            int rowTop = ClientServices.CLIENT.getAbstractListRowTop(list, index);
            int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(list, index);
            int listTop = ClientServices.CLIENT.getAbstractListTop(list);
            int listBottom = ClientServices.CLIENT.getAbstractListBottom(list);
            if(rowTop < listTop && rowBottom > listBottom)
                continue;

            Object entry = entries.get(index);
            if(entry instanceof ContainerEventHandler handler)
            {
                return findHoveredEventListenerExcludeList(handler, mouseX, mouseY);
            }
        }
        return Optional.empty();
    }

    private static Optional<GuiEventListener> findHoveredEventListenerInRecipeBook(ContainerEventHandler handler, double mouseX, double mouseY)
    {
        if(handler instanceof RecipeUpdateListener listener)
        {
            RecipeBookComponent recipeBook = listener.getRecipeBookComponent();
            if(recipeBook.isVisible())
            {
                List<GuiEventListener> listeners = new ArrayList<>();
                RecipeBookComponentAccessor bookAccessor = (RecipeBookComponentAccessor) recipeBook;
                listeners.add(bookAccessor.controllableGetFilterButton());
                listeners.addAll(bookAccessor.controllableGetRecipeTabs());
                RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) bookAccessor.controllableGetRecipeBookPage();
                listeners.addAll(pageAccessor.controllableGetButtons());
                listeners.add(pageAccessor.controllableGetForwardButton());
                listeners.add(pageAccessor.controllableGetBackButton());
                return listeners.stream().filter(o -> o != null && o.isMouseOver(mouseX, mouseY)).findFirst();
            }
        }
        return Optional.empty();
    }
}
