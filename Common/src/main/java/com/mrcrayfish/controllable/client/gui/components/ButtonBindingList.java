package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends TabSelectionList
{
    private final SettingsScreen settingsScreen;
    protected Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(SettingsScreen settingsScreen, Minecraft mc, int itemHeight)
    {
        super(mc, itemHeight);
        this.settingsScreen = settingsScreen;
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
        this.categories.put("key.categories.controllable_custom", new ArrayList<>());
        this.populate(false);
    }

    private void populate(boolean showUnbound)
    {
        // Clear the list of bindings for each category
        this.categories.forEach((category, list) -> list.clear());

        // Add all button bindings to the appropriate category or create a new one
        BindingRegistry.getInstance().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
        {
            // Only show unbound bindings for select binding screen for radial menu
            if(showUnbound && binding.getButton() != -1) return;
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

        protected ButtonBindingItem(ButtonBinding binding)
        {
            super(Component.translatable(binding.getLabelKey()));
            this.binding = binding;
            this.bindingButton = new ButtonBindingButton(0, 0, binding, button -> {
                if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    ButtonBindingList.this.settingsScreen.setSelectedBinding(this.binding);
                    return true;
                } else if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    this.binding.setButton(-1);
                    return true;
                }
                return false;
            });
            this.resetButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button -> {
                binding.reset();
                BindingRegistry registry = BindingRegistry.getInstance();
                registry.resetBindingHash();
                registry.save();
            });
        }

        private List<Component> getBindingTooltip(ButtonBinding binding)
        {
            if(Controllable.getInput().isControllerInUse())
            {
                List<Component> components = new ArrayList<>();
                components.add(Component.translatable("controllable.gui.change_binding", ClientHelper.getButtonComponent(Buttons.A)).withStyle(ChatFormatting.YELLOW));
                if(binding.getButton() != -1)
                {
                    components.add(Component.translatable("controllable.gui.clear_binding", ClientHelper.getButtonComponent(Buttons.X)).withStyle(ChatFormatting.YELLOW));
                }
                return components;
            }

            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("controllable.gui.change_binding", InputConstants.Type.MOUSE.getOrCreate(0).getDisplayName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
            if(binding.getButton() != -1)
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
        public void render(PoseStack poseStack, int index, int top, int left, int width, int itemHeight, int mouseX, int mouseY, boolean selected, float partialTick)
        {
            this.setLabelColor(this.binding.isConflictingContext() ? ChatFormatting.RED.getColor() : ChatFormatting.WHITE.getColor());
            super.render(poseStack, index, top, left, width, itemHeight, mouseX, mouseY, selected, partialTick);
            this.bindingButton.setTooltip(ClientHelper.createListTooltip(this.getBindingTooltip(this.binding)));
            this.bindingButton.setTooltipDelay(400);
            this.bindingButton.setX(left + width - 65);
            this.bindingButton.setY(top - 1);
            this.bindingButton.render(poseStack, mouseX, mouseY, partialTick);
            this.resetButton.setX(left + width - 24);
            this.resetButton.setY(top - 1);
            this.resetButton.active = !this.binding.isDefault();
            this.resetButton.render(poseStack, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(!this.resetButton.isHovered() && Controllable.getInput().isControllerInUse())
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
}
