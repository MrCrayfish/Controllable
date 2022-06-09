package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.ButtonBindingData;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SelectButtonBindingScreen extends ButtonBindingListMenuScreen
{
    public SelectButtonBindingScreen(RadialMenuConfigureScreen parentScreen)
    {
        super(parentScreen, Component.translatable("controllable.gui.title.select_button_bindings"), 22);
    }

    public RadialMenuConfigureScreen getRadialConfigureScreen()
    {
        return (RadialMenuConfigureScreen) this.parent;
    }

    @Override
    protected void init()
    {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, Component.translatable("controllable.gui.restoreDefaults"), (button) -> {
            this.minecraft.setScreen(new ConfirmationScreen(this, Component.translatable("controllable.gui.reset_selected_bindings"), result -> {
                if(result) {
                    ((RadialMenuConfigureScreen) this.parent).getBindings().clear();
                    ((RadialMenuConfigureScreen) this.parent).getBindings().addAll(RadialMenuHandler.instance().getDefaults());
                    this.list.children().stream().filter(entry -> entry instanceof ButtonBindingItem).map(entry -> (ButtonBindingItem) entry).forEach(ButtonBindingItem::updateButtons);
                }
                return true;
            }));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.parent);
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
            super(Component.translatable(binding.getLabelKey()));
            this.binding = binding;

            List<ButtonBindingData> bindings = getRadialConfigureScreen().getBindings();
            this.bindingButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
            {
                bindings.add(new ButtonBindingData(this.binding, ChatFormatting.YELLOW));
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
            return this.label.plainCopy().getString();
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(PoseStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            int color = this.binding.isConflictingContext() ? ChatFormatting.RED.getColor() : ChatFormatting.WHITE.getColor();
            SelectButtonBindingScreen.this.minecraft.font.draw(matrixStack, this.label, left - 15, y + 6, color);
            this.bindingButton.x = left + width - 37;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 15;
            this.deleteButton.y = y;
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
