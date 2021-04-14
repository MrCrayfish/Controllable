package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SelectKeyBindingScreen extends Screen
{
    private Screen parent;
    private KeyBindingList bindingList;
    private final ITextComponent note;
    private Button resetButton;

    public SelectKeyBindingScreen(Screen parent)
    {
        super(new TranslationTextComponent("controllable.gui.title.select_key_bindings"));
        this.note = new TranslationTextComponent("controllable.gui.note").mergeStyle(TextFormatting.RED).append(new TranslationTextComponent("controllable.gui.key_bind_note").mergeStyle(TextFormatting.GRAY));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        this.bindingList = new KeyBindingList(this, this.minecraft, this.width + 10, this.height, 45, this.height - 44, 20);
        this.children.add(this.bindingList);
        this.resetButton = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslationTextComponent("controllable.gui.reset"), (button) -> {
            List<KeyAdapterBinding> copy = new ArrayList<>(BindingRegistry.getInstance().getKeyAdapters().values());
            copy.forEach(binding -> {
                BindingRegistry.getInstance().removeKeyAdapter(binding);
                RadialMenuHandler.instance().removeBinding(binding);
            });
            this.bindingList.getEventListeners().stream().filter(entry -> entry instanceof KeyBindingList.KeyBindingEntry).map(entry -> (KeyBindingList.KeyBindingEntry) entry).forEach(KeyBindingList.KeyBindingEntry::updateButtons);
            this.updateButtons();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 29, 150, 20, DialogTexts.GUI_DONE, (button) -> {
            this.minecraft.displayGuiScreen(this.parent);
        }));
        this.updateButtons();
    }

    void updateButtons()
    {
        this.resetButton.active = BindingRegistry.getInstance().getKeyAdapters().size() > 0;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.bindingList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font, this.note, this.width / 2, 26, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
