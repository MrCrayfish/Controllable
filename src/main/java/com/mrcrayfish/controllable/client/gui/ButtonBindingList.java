package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends ContainerObjectSelectionList<ButtonBindingList.Entry>
{
    private Screen parent;
    private Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(Screen parent, Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);
        this.parent = parent;
        this.updateList(false);
    }

    public void updateList(boolean showUnbound)
    {
        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());
        this.categories.put("key.categories.controllable_custom", new ArrayList<>());

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
                this.addEntry(new CategoryEntry(new TranslatableComponent(category)));
                list.forEach(binding -> this.addEntry(new BindingEntry(binding)));
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.parent instanceof ButtonBindingScreen)
        {
            if(((ButtonBindingScreen) this.parent).isWaitingForButtonInput())
            {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {}

    protected class CategoryEntry extends Entry
    {
        private final Component label;
        private final int labelWidth;

        protected CategoryEntry(Component label)
        {
            this.label = label;
            this.labelWidth = ButtonBindingList.this.minecraft.font.width(this.label);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return Collections.emptyList();
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
                    output.add(NarratedElementType.TITLE, CategoryEntry.this.label);
                }
            });
        }

        @Override
        public boolean changeFocus(boolean focus)
        {
            return false;
        }

        @Override
        public void render(PoseStack poseStack, int x, int y, int p_230432_4_, int p_230432_5_, int itemHeight, int p_230432_7_, int p_230432_8_, boolean selected, float partialTicks)
        {
            float labelX = ButtonBindingList.this.minecraft.screen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            ButtonBindingList.this.minecraft.font.draw(poseStack, this.label, labelX, labelY, 0xFFFFFFFF);
        }
    }

    public class BindingEntry extends Entry
    {
        private ButtonBinding binding;
        private Component label;
        private Button bindingButton;
        private Button deleteButton;
        private Button removeButton;

        protected BindingEntry(ButtonBinding binding)
        {
            this.binding = binding;
            this.label = new TranslatableComponent(binding.getLabelKey());
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                this.bindingButton = new ButtonBindingButton(0, 0, binding, button ->
                {
                    if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
                    {
                        ((ButtonBindingScreen) ButtonBindingList.this.parent).setSelectedBinding(this.binding);
                    }
                });
                this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button ->
                {
                    binding.reset();
                    BindingRegistry registry = BindingRegistry.getInstance();
                    registry.resetBindingHash();
                    registry.save();
                });
                this.removeButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 0, 0, 16, 16, button -> {
                    if(binding instanceof KeyAdapterBinding) BindingRegistry.getInstance().removeKeyAdapter((KeyAdapterBinding) binding);
                    ButtonBindingList.this.removeEntry(this);
                });
                this.removeButton.visible = binding instanceof KeyAdapterBinding;
            }
            else if(ButtonBindingList.this.parent instanceof SelectButtonBindingScreen)
            {
                SelectButtonBindingScreen screen = (SelectButtonBindingScreen) ButtonBindingList.this.parent;
                List<ButtonBindingData> bindings = screen.getRadialConfigureScreen().getBindings();
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
        }

        public void updateButtons()
        {
            if(ButtonBindingList.this.parent instanceof SelectButtonBindingScreen)
            {
                SelectButtonBindingScreen screen = (SelectButtonBindingScreen) ButtonBindingList.this.parent;
                List<ButtonBindingData> bindings = screen.getRadialConfigureScreen().getBindings();
                this.bindingButton.active = bindings.stream().noneMatch(entry -> entry.getBinding() == this.binding);
                this.deleteButton.active = bindings.stream().anyMatch(entry -> entry.getBinding() == this.binding);
            }
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
            int color = this.binding.isConflictingContext() ? ChatFormatting.RED.getColor() : ChatFormatting.GRAY.getColor();
            ButtonBindingList.this.minecraft.font.draw(matrixStack, this.label, left - 15, y + 6, color);
            this.bindingButton.x = left + width - 38;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 15;
            this.deleteButton.y = y;
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                this.deleteButton.active = !this.binding.isDefault();
            }
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && this.bindingButton.isHovered())
                {
                    this.binding.setButton(-1);
                    this.bindingButton.playDownSound(Minecraft.getInstance().getSoundManager());
                    return true;
                }
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
                    output.add(NarratedElementType.TITLE, BindingEntry.this.label);
                }
            });
        }
    }
}
