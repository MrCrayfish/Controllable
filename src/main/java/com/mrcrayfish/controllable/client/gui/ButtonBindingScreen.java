package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ISearchable;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingScreen extends ButtonBindingListMenuScreen
{
    private Button buttonReset;
    private ButtonBinding selectedBinding = null;

    protected ButtonBindingScreen(Screen parentScreen)
    {
        super(parentScreen, new TranslationTextComponent("controllable.gui.title.button_binding"), 22);
    }

    void setSelectedBinding(ButtonBinding selectedBinding)
    {
        this.selectedBinding = selectedBinding;
    }

    boolean isWaitingForButtonInput()
    {
        return this.selectedBinding != null;
    }

    @Override
    protected void init()
    {
        super.init();
        this.buttonReset = this.addButton(new Button(this.width / 2 - 155, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.resetBinds"), (button) -> {
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.getBindings().forEach(ButtonBinding::reset);
            registry.resetBindingHash();
            registry.save();
        }));
        this.buttonReset.active = BindingRegistry.getInstance().getBindings().stream().noneMatch(ButtonBinding::isDefault);

        this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.add_key_bind"), button -> {
            Objects.requireNonNull(this.minecraft).displayGuiScreen(new SelectKeyBindingScreen(this));
        }));

        this.addButton(new Button(this.width / 2 + 55, this.height - 32, 100, 20, DialogTexts.GUI_DONE, (button) -> {
            Objects.requireNonNull(this.minecraft).displayGuiScreen(this.parent);
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.resetBindingHash();
            registry.save();
        }));
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        this.updateList(entries, false);
    }

    @Override
    public void tick()
    {
        this.buttonReset.active = !BindingRegistry.getInstance().getBindings().stream().allMatch(ButtonBinding::isDefault);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, this.selectedBinding == null ? mouseX : -1, this.selectedBinding == null ? mouseY : -1, partialTicks);

        if(this.selectedBinding != null)
        {
            RenderSystem.disableDepthTest();
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
            drawCenteredString(matrixStack, this.font, new TranslationTextComponent("controllable.gui.layout.press_button"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.selectedBinding != null)
        {
            if(this.isWaitingForButtonInput())
            {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        if(key == GLFW.GLFW_KEY_ESCAPE && this.selectedBinding != null)
        {
            this.selectedBinding = null;
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
    }

    public boolean processButton(int index)
    {
        if(this.selectedBinding != null)
        {
            this.selectedBinding.setButton(index);
            this.selectedBinding = null;
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.resetBindingHash();
            registry.save();
            return true;
        }
        return false;
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
        private final Button deleteButton;
        private final Button removeButton;

        protected ButtonBindingItem(ButtonBinding binding)
        {
            super(new TranslationTextComponent(binding.getLabelKey()));
            this.binding = binding;
            this.bindingButton = new ButtonBindingButton(0, 0, binding, button -> ButtonBindingScreen.this.setSelectedBinding(this.binding));
            this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button ->
            {
                binding.reset();
                BindingRegistry registry = BindingRegistry.getInstance();
                registry.resetBindingHash();
                registry.save();
            });
            this.removeButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 0, 0, 16, 16, button -> {
                if(binding instanceof KeyAdapterBinding) BindingRegistry.getInstance().removeKeyAdapter((KeyAdapterBinding) binding);
                ButtonBindingScreen.this.list.removeEntry(this);
            });
            this.removeButton.visible = binding instanceof KeyAdapterBinding;
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
            ButtonBindingScreen.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left - 15, y + 6, color);
            this.bindingButton.x = left + width - 37;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 15;
            this.deleteButton.y = y;
            this.deleteButton.active = !this.binding.isDefault();
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && this.bindingButton.isHovered())
            {
                this.binding.setButton(-1);
                this.bindingButton.playDownSound(Minecraft.getInstance().getSoundHandler());
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
