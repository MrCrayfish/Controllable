package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

import java.util.List;
import java.util.function.Function;

/**
 * A simple versatile confirmation screen
 * <p>
 * Author: MrCrayfish
 */
public class ConfirmationScreen extends Screen
{
    private final Screen parent;
    private final ITextComponent message;
    private final Function<Boolean, Boolean> handler;
    private ITextComponent positiveText = DialogTexts.GUI_YES;
    private ITextComponent negativeText = DialogTexts.GUI_NO;

    public ConfirmationScreen(Screen parent, ITextComponent message, Function<Boolean, Boolean> handler)
    {
        super(message);
        this.parent = parent;
        this.message = message;
        this.handler = handler;
    }

    @Override
    protected void init()
    {
        List<IReorderingProcessor> lines = this.font.trimStringToWidth(this.message, 300);
        int messageOffset = (lines.size() * (this.font.FONT_HEIGHT + 2)) / 2;
        this.addButton(new Button(this.width / 2 - 105, this.height / 2 + messageOffset, 100, 20, this.positiveText, button ->
        {
            if(this.handler.apply(true))
            {
                this.minecraft.displayGuiScreen(this.parent);
            }
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height / 2 + messageOffset, 100, 20, this.negativeText, button ->
        {
            if(this.handler.apply(false))
            {
                this.minecraft.displayGuiScreen(this.parent);
            }
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        List<ITextProperties> lines = this.font.getCharacterManager().func_238362_b_(this.message, 200, Style.EMPTY);
        for(int i = 0; i < lines.size(); i++)
        {
            drawCenteredString(matrixStack, this.font, lines.get(i).getString(), this.width / 2, this.height / 2 - 20 - (lines.size() * (this.font.FONT_HEIGHT + 2)) / 2 + i * (this.font.FONT_HEIGHT + 2), 0xFFFFFF);
        }
    }

    /**
     * Sets the text for the positive button. This must be called before the screen is displayed.
     *
     * @param positiveText the text component to display as the positive button label
     */
    public void setPositiveText(ITextComponent positiveText)
    {
        this.positiveText = positiveText;
    }

    /**
     * Sets the text for the negative button. This must be called before the screen is displayed.
     *
     * @param negativeText the text component to display as the negative button label
     */
    public void setNegativeText(ITextComponent negativeText)
    {
        this.negativeText = negativeText;
    }
}
