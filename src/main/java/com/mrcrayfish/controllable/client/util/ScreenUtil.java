package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.client.ISearchable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ScreenUtil
{
    /**
     * Determines if the mouse is within the specified area.
     *
     * @param x      the x position of the area
     * @param y      the y position of the area
     * @param width  the width of the area
     * @param height the height of the area
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     * @return true if the mouse is within the area
     */
    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * A helper method to create a button tooltip. The tooltip will only render if the button is
     * active and is hovered by the cursor.
     *
     * @param screen   the screen that will render the tooltip
     * @param message  the message to show in the tooltip
     * @param maxWidth the maximum text wrap width
     * @return a new button tooltip instance
     */
    public static Button.ITooltip createButtonTooltip(Screen screen, ITextComponent message, int maxWidth)
    {
        return createButtonTooltip(screen, message, maxWidth, button -> button.active && button.isHovered());
    }

    /**
     * Creates a button tooltip but only shows if the given predicate is true
     *
     * @param screen    the screen that will render the tooltip
     * @param message   the message to show in the tooltip
     * @param maxWidth  the maximum text wrap width
     * @param predicate the condition to determine if the tooltip should render
     * @return a new button tooltip instance
     */
    public static Button.ITooltip createButtonTooltip(Screen screen, ITextComponent message, int maxWidth, Predicate<Button> predicate)
    {
        return (button, matrixStack, mouseX, mouseY) ->
        {
            if(predicate.test(button))
            {
                screen.renderTooltip(matrixStack, Minecraft.getInstance().fontRenderer.trimStringToWidth(message, maxWidth), mouseX, mouseY);
            }
        };
    }

    /**
     * Updates the suggestion of a {@link TextFieldWidget} based on a list of label providers.
     *
     * @param textField the text field to update the suggestion
     * @param value     the user input value
     * @param entries   a list of label providers to test the user input against
     */
    public static void updateSearchTextFieldSuggestion(TextFieldWidget textField, String value, List<? extends ISearchable> entries)
    {
        if(!value.isEmpty())
        {
            Optional<? extends ISearchable> optional = entries.stream().filter(info -> info.getLabel().toLowerCase(Locale.ENGLISH).startsWith(value.toLowerCase(Locale.ENGLISH))).min(Comparator.comparing(ISearchable::getLabel));
            if(optional.isPresent())
            {
                int length = value.length();
                String displayName = optional.get().getLabel();
                textField.setSuggestion(displayName.substring(length));
            }
            else
            {
                textField.setSuggestion("");
            }
        }
        else
        {
            textField.setSuggestion(new TranslationTextComponent("controllable.gui.search").getString());
        }
    }
}