package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class RadialMenuConfigureScreen extends Screen
{
    private List<ButtonBindingData> bindings;
    private RadialItemList list;

    public RadialMenuConfigureScreen(LinkedHashSet<ButtonBindingData> bindings)
    {
        super(new TranslationTextComponent("controllable.gui.title.radial_menu_configure"));
        this.bindings = new ArrayList<>(bindings);
    }

    @Override
    protected void init()
    {
        this.list = new RadialItemList(this.minecraft, this.width, this.height, 45, this.height - 44, this.bindings);
        this.children.add(this.list);
        this.addButton(new Button(this.width / 2 + 5, this.height - 29, 150, 20, DialogTexts.GUI_DONE, buttons -> {
            RadialMenuHandler.instance().setBindings(new LinkedHashSet<>(this.bindings));
            Objects.requireNonNull(this.minecraft).displayGuiScreen(null);
        }));
        this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslationTextComponent("controllable.gui.add_binding"), buttons -> {
            Objects.requireNonNull(this.minecraft).displayGuiScreen(new SelectButtonBindingScreen(this));
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public List<ButtonBindingData> getBindings()
    {
        return this.bindings;
    }
}
