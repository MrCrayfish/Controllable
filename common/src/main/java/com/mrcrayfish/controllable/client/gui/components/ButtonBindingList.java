package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.RadialMenuConfigureScreen;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.gui.ISearchable;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.gui.screens.ConfirmationScreen;
import com.mrcrayfish.controllable.client.gui.screens.SelectKeyBindingScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends TabSelectionList<TabSelectionList.BaseItem>
{
    private final SettingsScreen settingsScreen;
    protected Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(SettingsScreen settingsScreen, Minecraft mc, int itemHeight)
    {
        super(mc, itemHeight);
        this.settingsScreen = settingsScreen;
        this.categories.put("key.categories.controllable_custom", new ArrayList<>());
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
        this.repopulateBindings(false);
    }

    private void repopulateBindings(boolean showUnbound)
    {
        this.clearEntries();

        Component addKeybind = ClientHelper.join(Icons.KEY_CAP, Component.translatable("controllable.gui.add_key_bind"));
        Component restoreDefaults = ClientHelper.join(Icons.RESET, Component.translatable("controllable.gui.restore_defaults"));
        this.addEntry(new TwoWidgetItem(Button.builder(addKeybind, btn -> {
            this.minecraft.setScreen(new SelectKeyBindingScreen(this.settingsScreen, () -> {
                this.repopulateBindings(false);
            }));
        }).build(), Button.builder(restoreDefaults, btn -> {
            this.minecraft.setScreen(new ConfirmationScreen(this.settingsScreen, Component.translatable("controllable.gui.reset_selected_bindings"), result -> {
                if(result) {
                    BindingRegistry registry = Controllable.getBindingRegistry();
                    registry.getBindings().forEach(ButtonBinding::resetMappedButton);
                    registry.rebuildCache();
                    registry.save();
                }
                return true;
            }));
        }).build()));

        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Add all button bindings to the appropriate category or create a new one
        Controllable.getBindingRegistry().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
        {
            // Only show unbound bindings for select binding screen for radial menu
            if(showUnbound && !binding.isUnbound()) return;
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                this.addEntry(new TitleItem(Component.translatable(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                list.forEach(binding -> this.addEntry(new ButtonBindingItem(binding)));
            }
        });
    }

    public class ButtonBindingItem extends TabOptionBaseItem implements ISearchable
    {
        private final ButtonBinding binding;
        private final Button bindingButton;
        private final Button resetButton;
        private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

        protected ButtonBindingItem(ButtonBinding binding)
        {
            super(Component.translatable(binding.getLabelKey()));
            this.binding = binding;
            this.tooltip.setDelay(Duration.ofMillis(400));
            this.bindingButton = new ButtonBindingButton(0, 0, binding, button -> {
                if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    ButtonBindingList.this.settingsScreen.setSelectedBinding(this.binding);
                    return true;
                } else if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    ButtonBinding.setButton(this.binding, -1);
                    BindingRegistry registry = Controllable.getBindingRegistry();
                    registry.rebuildCache();
                    registry.save();
                    return true;
                }
                return false;
            });
            this.resetButton = new ImageButton(0, 0, 20, Icons.TEXTURE, 44, 0, 11, 11, Icons.TEXTURE_WIDTH, Icons.TEXTURE_HEIGHT, button -> {
                binding.resetMappedButton();
                BindingRegistry registry = Controllable.getBindingRegistry();
                registry.rebuildCache();
                registry.save();
            });
        }

        private List<Component> getBindingTooltip(ButtonBinding binding)
        {
            Controller controller = Controllable.getController();
            if(controller != null && controller.isBeingUsed())
            {
                List<Component> components = new ArrayList<>();
                components.add(Component.translatable("controllable.gui.change_binding", ClientHelper.getButtonComponent(Buttons.A)).withStyle(ChatFormatting.YELLOW));
                if(!binding.isUnbound())
                {
                    components.add(Component.translatable("controllable.gui.clear_binding", ClientHelper.getButtonComponent(Buttons.X)).withStyle(ChatFormatting.YELLOW));
                }
                return components;
            }

            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("controllable.gui.change_binding", InputConstants.Type.MOUSE.getOrCreate(0).getDisplayName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
            if(!binding.isUnbound())
            {
                components.add(Component.translatable("controllable.gui.clear_binding", InputConstants.Type.MOUSE.getOrCreate(1).getDisplayName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
            }
            return components;
        }

        @Override
        public Component getLabel()
        {
            return this.label;
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.bindingButton, this.resetButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int itemHeight, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.updateTooltip(mouseX, mouseY);
            this.setLabelColor(this.binding.isConflictingContext() ? ChatFormatting.RED.getColor() : ChatFormatting.WHITE.getColor());
            super.render(graphics, index, top, left, width, itemHeight, mouseX, mouseY, selected, partialTick);
            this.bindingButton.setTooltip(ClientHelper.createListTooltip(this.getBindingTooltip(this.binding)));
            this.bindingButton.setTooltipDelay(Duration.ofMillis(400));
            this.bindingButton.setX(left + width - 65);
            this.bindingButton.setY(top);
            this.bindingButton.render(graphics, mouseX, mouseY, partialTick);
            this.resetButton.setX(left + width - 24);
            this.resetButton.setY(top);
            this.resetButton.active = !this.binding.isDefault();
            this.resetButton.render(graphics, mouseX, mouseY, partialTick);
        }

        private void updateTooltip(double mouseX, double mouseY)
        {
            Controller controller = Controllable.getController();
            if(!this.bindingButton.isHovered() && !this.resetButton.isHovered() && this.isMouseOver(mouseX, mouseY) && controller != null && controller.isBeingUsed())
            {
                this.tooltip.set(ClientHelper.createListTooltip(this.getBindingTooltip(this.binding)));
            }
            else
            {
                this.tooltip.set(null);
            }
            this.tooltip.refreshTooltipForNextRenderPass(true, false, this.getRectangle());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            Controller controller = Controllable.getController();
            if(!this.resetButton.isHovered() && controller != null && controller.isBeingUsed())
            {
                this.bindingButton.mouseClicked(this.bindingButton.getX(), this.bindingButton.getY(), button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public List<? extends NarratableEntry> narratables()
        {
            return ImmutableList.of(new NarratableEntry()
            {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority()
                {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output)
                {
                    output.add(NarratedElementType.TITLE, ButtonBindingItem.this.label);
                }
            });
        }
    }

    public static class OneWidgetItem extends BaseItem
    {
        private final AbstractWidget widget;

        public OneWidgetItem(AbstractWidget widget)
        {
            super(CommonComponents.EMPTY);
            this.widget = widget;
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.widget.setWidth(width - 10);
            this.widget.setX(left + 5);
            this.widget.setY(top);
            this.widget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.widget);
        }
    }

    public static class TwoWidgetItem extends BaseItem
    {
        private final AbstractWidget leftWidget;
        private final AbstractWidget rightWidget;

        public TwoWidgetItem(AbstractWidget leftWidget, AbstractWidget rightWidget)
        {
            super(CommonComponents.EMPTY);
            this.leftWidget = leftWidget;
            this.rightWidget = rightWidget;
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.leftWidget.setWidth(width / 2 - 10);
            this.leftWidget.setX(left + 5);
            this.leftWidget.setY(top);
            this.leftWidget.render(graphics, mouseX, mouseY, partialTick);
            this.rightWidget.setWidth(width / 2 - 10);
            this.rightWidget.setX(left + width / 2 + 5);
            this.rightWidget.setY(top);
            this.rightWidget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.leftWidget, this.rightWidget);
        }
    }
}
