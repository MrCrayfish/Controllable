package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingScreen extends Screen
{
    private Screen parentScreen;
    private ButtonBindingList bindingList;

    protected ButtonBindingScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.button_binding"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
        this.bindingList = new ButtonBindingList(this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.bindingList);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.bindingList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
