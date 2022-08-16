package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SelectButtonBindingScreen extends ButtonBindingListMenuScreen
{
    public SelectButtonBindingScreen(RadialMenuConfigureScreen parentScreen)
    {
        super(parentScreen, new TranslationTextComponent("controllable.gui.title.select_button_bindings"), 22);
    }

    public RadialMenuConfigureScreen getRadialConfigureScreen()
    {
        return (RadialMenuConfigureScreen) this.parent;
    }

    @Override
    protected void init()
    {
        super.init();
        this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslationTextComponent("controllable.gui.restoreDefaults"), (button) -> {
            this.minecraft.displayGuiScreen(new ConfirmationScreen(this, new TranslationTextComponent("controllable.gui.reset_selected_bindings"), result -> {
                if(result) {
                    ((RadialMenuConfigureScreen) this.parent).getBindings().clear();
                    ((RadialMenuConfigureScreen) this.parent).getBindings().addAll(RadialMenuHandler.instance().getDefaults());
                    this.list.getEventListeners().stream().filter(entry -> entry instanceof ButtonBindingItem).map(entry -> (ButtonBindingItem) entry).forEach(ButtonBindingItem::updateButtons);
                }
                return true;
            }));
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 29, 150, 20, DialogTexts.GUI_DONE, (button) -> {
            this.minecraft.displayGuiScreen(this.parent);
        }));
    }

    @Override
    protected Item createItemFromBinding(ButtonBinding binding)
    {
        return new ButtonBindingItem(binding);
    }

    public class ButtonBindingItem extends Item implements ISearchable
    {
        private final ButtonBinding binding;
        private Button bindingButton;
        private Button deleteButton;

        protected ButtonBindingItem(ButtonBinding binding)
        {
            super(new TranslationTextComponent(binding.getLabelKey()));
            this.binding = binding;

            List<ButtonBindingData> bindings = getRadialConfigureScreen().getBindings();
            this.bindingButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
            {
                bindings.add(new ButtonBindingData(this.binding, TextFormatting.YELLOW));
                this.bindingButton.active = false;
                this.deleteButton.active = true;
            });
            this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 15, 10, 10, button ->
            {
                bindings.removeIf(entry -> entry.getBinding() == this.binding);
                this.bindingButton.active = true;
                this.deleteButton.active = false;
            });
            this.bindingButton.active = bindings.stream().noneMatch(entry -> entry.getBinding() == this.binding);
            this.deleteButton.active = bindings.stream().anyMatch(entry -> entry.getBinding() == this.binding);
        }

        public void updateButtons()
        {
            List<ButtonBindingData> bindings = getRadialConfigureScreen().getBindings();
            this.bindingButton.active = bindings.stream().noneMatch(entry -> entry.getBinding() == this.binding);
            this.deleteButton.active = bindings.stream().anyMatch(entry -> entry.getBinding() == this.binding);
        }

        @Override
        public String getLabel()
        {
            return this.label.copyRaw().getString();
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(MatrixStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            int color = this.binding.isConflictingContext() ? TextFormatting.RED.getColor() : TextFormatting.WHITE.getColor();
            SelectButtonBindingScreen.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left - 15, y + 6, color);
            this.bindingButton.x = left + width - 37;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 15;
            this.deleteButton.y = y;
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
