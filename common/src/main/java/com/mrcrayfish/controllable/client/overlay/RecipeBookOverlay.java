package com.mrcrayfish.controllable.client.overlay;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RecipeBookOverlay implements IOverlay
{
    private RecipeBookComponent recipeBook;

    @Override
    public boolean isVisible()
    {
        return Controllable.getInput().isControllerInUse() && this.recipeBook != null && this.recipeBook.isVisible();
    }

    @Override
    public void tick()
    {
        this.recipeBook = null;
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if(screen instanceof RecipeUpdateListener listener)
        {
            this.recipeBook = listener.getRecipeBookComponent();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        Font font = Minecraft.getInstance().font;

        List<RecipeBookTabButton> tabButtons = ((RecipeBookComponentAccessor) this.recipeBook).controllableGetTabButtons();
        if(!tabButtons.isEmpty())
        {
            RecipeBookTabButton first = tabButtons.get(0);
            RecipeBookTabButton last = tabButtons.get(tabButtons.size() - 1);
            graphics.drawString(font, ClientHelper.getButtonComponent(ButtonBindings.NEXT_RECIPE_TAB.getButton()), first.getX() + 15 - 5, first.getY() - 13, 0xFFFFFF);
            graphics.drawString(font, ClientHelper.getButtonComponent(ButtonBindings.PREVIOUS_RECIPE_TAB.getButton()), last.getX() + 15 - 5, last.getY() + last.getHeight() + 13 - 9, 0xFFFFFF);
        }

        RecipeBookPage page = ((RecipeBookComponentAccessor) this.recipeBook).controllableGetRecipeBookPage();

        StateSwitchingButton forwardButton = ((RecipeBookPageAccessor) page).controllableGetForwardButton();
        if(forwardButton.visible)
        {
            graphics.drawString(font, ClientHelper.getButtonComponent(ButtonBindings.PREVIOUS_CREATIVE_TAB.getButton()), forwardButton.getX() + 24 - 5, forwardButton.getY() + 4, 0xFFFFFF);
        }

        StateSwitchingButton backButton = ((RecipeBookPageAccessor) page).controllableGetBackButton();
        if(backButton.visible)
        {
            graphics.drawString(font, ClientHelper.getButtonComponent(ButtonBindings.NEXT_CREATIVE_TAB.getButton()), backButton.getX() - 24 + 12 - 5, backButton.getY() + 4, 0xFFFFFF);
        }
    }
}
