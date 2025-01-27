package com.mrcrayfish.controllable.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.gui.ISearchable;
import com.mrcrayfish.controllable.client.gui.RadialMenuAction;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class RadialMenuAddBindingsScreen extends ButtonBindingListMenuScreen
{
    public RadialMenuAddBindingsScreen(RadialMenuConfigureScreen parentScreen)
    {
        super(parentScreen, Component.translatable("controllable.gui.title.select_button_bindings"), 24);
        this.setRowWidth(290);
    }

    public RadialMenuConfigureScreen getRadialConfigureScreen()
    {
        return (RadialMenuConfigureScreen) this.parent;
    }

    @Override
    protected void setupFooter(LinearLayout footerLayout)
    {
        super.setupFooter(footerLayout);
        Component resetLabel = ClientHelper.join(Icons.RESET, Component.translatable("controllable.gui.restore_defaults"));
        footerLayout.addChild(ScreenHelper.button(this.width / 2 - 155, this.height - 29, 150, 20, resetLabel, (button) -> {
            Objects.requireNonNull(this.minecraft).setScreen(new ConfirmationScreen(this, Component.translatable("controllable.gui.reset_selected_bindings"), result -> {
                if(result) {
                    RadialMenuConfigureScreen screen = getRadialConfigureScreen();
                    screen.getActions().clear();
                    screen.getActions().addAll(Controllable.getRadialMenu().createDefaultActions());
                    this.list.children().forEach(item -> {
                        if(item instanceof ButtonBindingItem bindingItem) {
                            bindingItem.updateActiveState();
                        }
                    });
                }
                return true;
            }));
        }));
        footerLayout.addChild(ScreenHelper.button(this.width / 2 + 5, this.height - 29, 150, 20, CommonComponents.GUI_BACK, (button) -> {
            Objects.requireNonNull(this.minecraft).setScreen(this.parent);
        }));
    }

    @Override
    protected void repositionElements()
    {
        getRadialConfigureScreen().getActions().removeIf(action -> {
            if(action.getBinding() instanceof KeyAdapterBinding adapter) {
                return !Controllable.getBindingRegistry().getKeyAdapters().containsValue(adapter);
            }
            return false;
        });
        super.repositionElements();
    }

    @Override
    protected Item createItemFromBinding(ButtonBinding binding)
    {
        return new ButtonBindingItem(binding);
    }

    public class ButtonBindingItem extends Item implements ISearchable
    {
        private final ButtonBinding binding;
        private final Button bindingButton;
        private boolean active;

        protected ButtonBindingItem(ButtonBinding binding)
        {
            super(Component.translatable(binding.getLabelKey()));
            this.binding = binding;
            List<RadialMenuAction> bindings = getRadialConfigureScreen().getActions();
            this.bindingButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button -> {
                bindings.add(new RadialMenuAction(this.binding, ChatFormatting.YELLOW));
                Objects.requireNonNull(RadialMenuAddBindingsScreen.this.minecraft).setScreen(RadialMenuAddBindingsScreen.this.parent);
                getRadialConfigureScreen().scrollToBottomAndSelectLast();
            });
            this.bindingButton.setTooltipDelay(Duration.ofMillis(400));
            this.updateActiveState();
        }

        @Override
        public Component getLabel()
        {
            return this.label;
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.bindingButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            // Draws a transparent black background on every odd item to help match the widgets with the label
            if(index % 2 != 0)
            {
                graphics.fill(left - 2, top - 2, left + rowWidth + 2, top + rowHeight + 2, 0x55000000);
            }
            Font font = RadialMenuAddBindingsScreen.this.minecraft.font;
            int colour = this.active ? ChatFormatting.WHITE.getColor() : ChatFormatting.DARK_GRAY.getColor();
            graphics.drawString(font, this.label, left + 5, top + 6, colour);
            this.bindingButton.setX(left + rowWidth - 25);
            this.bindingButton.setY(top);
            this.bindingButton.render(graphics, mouseX, mouseY, partialTicks);
            this.bindingButton.active = this.active;
        }

        public void updateActiveState()
        {
            this.active = getRadialConfigureScreen().getActions().stream().noneMatch(data -> {
                return data.getBinding() == binding;
            });
            Tooltip addTooltip = this.active ?
                Tooltip.create(Component.translatable("controllable.gui.add_to_radial_menu")) :
                Tooltip.create(Component.translatable("controllable.gui.binding_already_added").withStyle(ChatFormatting.RED));
            this.bindingButton.setTooltip(addTooltip);
        }
    }
}
