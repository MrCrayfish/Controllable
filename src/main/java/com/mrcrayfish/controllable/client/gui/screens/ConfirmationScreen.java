package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

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
    private final Component message;
    private final Function<Boolean, Boolean> handler;
    private Component positiveText = CommonComponents.GUI_YES;
    private Component negativeText = CommonComponents.GUI_NO;

    public ConfirmationScreen(Screen parent, Component message, Function<Boolean, Boolean> handler)
    {
        super(message);
        this.parent = parent;
        this.message = message;
        this.handler = handler;
    }

    @Override
    protected void init()
    {
        List<FormattedCharSequence> lines = this.font.split(this.message, 300);
        int messageOffset = (lines.size() * (this.font.lineHeight + 2)) / 2;
        this.addRenderableWidget(new Button(this.width / 2 - 105, this.height / 2 + messageOffset, 100, 20, this.positiveText, button ->
        {
            if(this.handler.apply(true))
            {
                this.minecraft.setScreen(this.parent);
            }
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 2 + messageOffset, 100, 20, this.negativeText, button ->
        {
            if(this.handler.apply(false))
            {
                this.minecraft.setScreen(this.parent);
            }
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        List<FormattedCharSequence> lines = this.font.split(this.message, 300);
        for(int i = 0; i < lines.size(); i++)
        {
            int lineWidth = this.font.width(lines.get(i));
            this.font.draw(poseStack, lines.get(i), this.width / 2 - lineWidth / 2, this.height / 2 - 20 - (lines.size() * (this.font.lineHeight + 2)) / 2 + i * (this.font.lineHeight + 2), 0xFFFFFF);
        }
    }

    /**
     * Sets the text for the positive button. This must be called before the screen is displayed.
     *
     * @param positiveText the text component to display as the positive button label
     */
    public void setPositiveText(Component positiveText)
    {
        this.positiveText = positiveText;
    }

    /**
     * Sets the text for the negative button. This must be called before the screen is displayed.
     *
     * @param negativeText the text component to display as the negative button label
     */
    public void setNegativeText(Component negativeText)
    {
        this.negativeText = negativeText;
    }
}
