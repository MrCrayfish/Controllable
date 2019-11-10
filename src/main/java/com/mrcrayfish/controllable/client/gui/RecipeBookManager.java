package com.mrcrayfish.controllable.client.gui;

import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeTabToggleWidget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeBookManager
{
    private final Field fRecipeBookPage = ObfuscationReflectionHelper.findField(RecipeBookGui.class, "field_193022_s");
    private final Field fCurrentPage = ObfuscationReflectionHelper.findField(RecipeBookPage.class, "field_193738_c");
    private final Field fTotalPages = ObfuscationReflectionHelper.findField(RecipeBookPage.class, "field_193737_b");
    private final Method mUpdateButtonsForPage = ObfuscationReflectionHelper.findMethod(RecipeBookPage.class, "func_194198_d");

    private final Field fRecipeTabs = ObfuscationReflectionHelper.findField(RecipeBookGui.class, "field_193018_j");
    private final Field fCurrentTab = ObfuscationReflectionHelper.findField(RecipeBookGui.class, "field_191913_x");
    private final Method mUpdateCollections = ObfuscationReflectionHelper.findMethod(RecipeBookGui.class, "func_193003_g", boolean.class);

    private final Method mIsOffsetNextToMainGUI = ObfuscationReflectionHelper.findMethod(RecipeBookGui.class, "func_191880_f");

    private final Field fToggleRecipesBtn = ObfuscationReflectionHelper.findField(RecipeBookGui.class, "field_193960_m");
    private final Method mToggleCraftableFilter = ObfuscationReflectionHelper.findMethod(RecipeBookGui.class, "func_201521_f");
    private final Method mSendUpdateSettings = ObfuscationReflectionHelper.findMethod(RecipeBookGui.class, "func_193956_j");

    private final Field fGuiLeft = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_147003_i");
    private final Field fXSize = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_146999_f");

    public RecipeBookManager()
    {
        fRecipeBookPage.setAccessible(true);
        fCurrentPage.setAccessible(true);
        fTotalPages.setAccessible(true);
        mUpdateButtonsForPage.setAccessible(true);

        fRecipeTabs.setAccessible(true);
        fCurrentTab.setAccessible(true);
        mUpdateCollections.setAccessible(true);

        mIsOffsetNextToMainGUI.setAccessible(true);

        fToggleRecipesBtn.setAccessible(true);
        mToggleCraftableFilter.setAccessible(true);
        mSendUpdateSettings.setAccessible(true);

        fGuiLeft.setAccessible(true);
    }

    public int getTotalPages(RecipeBookGui screen) {
        try {
            RecipeBookPage page = (RecipeBookPage) fRecipeBookPage.get(screen);
            return fTotalPages.getInt(page);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int getTabCount(RecipeBookGui screen)
    {
        try {
            return ((List)fRecipeTabs.get(screen)).size();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void nextPage(RecipeBookGui screen)
    {
        changePage(screen, 1);
    }

    public void prevPage(RecipeBookGui screen)
    {
        changePage(screen, -1);
    }

    private void changePage(RecipeBookGui screen, int amount)
    {
        try {
            RecipeBookPage page = (RecipeBookPage) fRecipeBookPage.get(screen);
            int currentPage = fCurrentPage.getInt(page);
            int totalPages = fTotalPages.getInt(page);
            fCurrentPage.setInt(page, MathHelper.clamp(currentPage + amount, 0, totalPages - 1));
            mUpdateButtonsForPage.invoke(page);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void nextTab(RecipeBookGui screen)
    {
        changeTab(screen, 1);
    }

    public void prevTab(RecipeBookGui screen)
    {
        changeTab(screen, -1);
    }

    private void changeTab(RecipeBookGui screen, int amount)
    {
        try {
            List<RecipeTabToggleWidget> tabs = (List<RecipeTabToggleWidget>) fRecipeTabs.get(screen);
            RecipeTabToggleWidget currentTab = (RecipeTabToggleWidget) fCurrentTab.get(screen);
            int i = MathHelper.clamp(tabs.indexOf(currentTab) + amount, 0, tabs.size() - 1);
            while (!tabs.get(i).active || !tabs.get(i).visible)
            {
                i = MathHelper.clamp(i + amount, 0, tabs.size() - 1);
            }
            RecipeTabToggleWidget newTab = tabs.get(i);
            currentTab.setStateTriggered(false);
            fCurrentTab.set(screen, tabs.get(i));
            newTab.setStateTriggered(true);
            mUpdateCollections.invoke(screen, true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean isOffsetNextToMainGUI(RecipeBookGui screen)
    {
        try {
            return (boolean) mIsOffsetNextToMainGUI.invoke(screen);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void toggleCraftableFilter(RecipeBookGui screen) {
        try {
            boolean flag = (boolean) mToggleCraftableFilter.invoke(screen);
            ToggleWidget toggleRecipesBtn = (ToggleWidget) fToggleRecipesBtn.get(screen);
            toggleRecipesBtn.setStateTriggered(flag);
            mSendUpdateSettings.invoke(screen);
            mUpdateCollections.invoke(screen, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void toggleRecipeBook(ContainerScreen screen) {
        try {
            RecipeBookGui book = ((IRecipeShownListener) screen).func_194310_f();
            book.toggleVisibility();
            int guiLeft = book.updateScreenPosition(screen.width < 379, screen.width, fXSize.getInt(screen));
            fGuiLeft.setInt(screen, guiLeft);
            ((ImageButton)screen.children().stream().filter(e -> e instanceof ImageButton).collect(Collectors.toList()).get(0)).setPosition(guiLeft + 5, screen.height / 2 - 49);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
