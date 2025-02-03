package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.ButtonIcons;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingButton extends Button
{
    private final ButtonBinding binding;
    private final ButtonOnPress onPress;
    private int lastButton;
    private boolean usingController;

    public ButtonBindingButton(int x, int y, ButtonBinding binding, ButtonOnPress onPress)
    {
        super(x, y, 40, 20, CommonComponents.EMPTY, btn -> {}, DEFAULT_NARRATION);
        this.binding = binding;
        this.onPress = onPress;
        this.lastButton = binding.getButton();
        this.updateTooltip(true);
    }

    public ButtonBinding getBinding()
    {
        return this.binding;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.updateTooltip(false);
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        if(this.binding.getButton() < 0)
            return;
        int texU = this.binding.getButton() * 13;
        int texV = Config.CLIENT.client.options.controllerIcons.get().ordinal() * 13;
        int size = 13;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(ButtonIcons.TEXTURE, this.getX() + (this.width - size) / 2 + 1, this.getY() + 3, texU, texV, size, size, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.active && this.visible && this.clicked(mouseX, mouseY))
        {
            if(this.onPress.onPress(button))
            {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return true;
        }
        return false;
    }

    private void updateTooltip(boolean force)
    {
        if(this.binding.getButton() != this.lastButton || this.usingController != Controllable.getInput().isControllerInUse() || force)
        {
            this.setTooltip(ClientHelper.createListTooltip(this.createBindingTooltip()));
            this.setTooltipDelay(400);
            this.lastButton = this.binding.getButton();
            this.usingController = Controllable.getInput().isControllerInUse();
        }
    }

    private List<Component> createBindingTooltip()
    {
        if(Controllable.getInput().isControllerInUse())
        {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("controllable.gui.change_binding", ClientHelper.getButtonComponent(Buttons.A)).withStyle(ChatFormatting.YELLOW));
            if(this.binding.getButton() != -1)
            {
                components.add(Component.translatable("controllable.gui.clear_binding", ClientHelper.getButtonComponent(Buttons.X)).withStyle(ChatFormatting.YELLOW));
            }
            return components;
        }

        List<Component> components = new ArrayList<>();
        components.add(Component.translatable("controllable.gui.change_binding", InputConstants.Type.MOUSE.getOrCreate(0).getDisplayName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
        if(this.binding.getButton() != -1)
        {
            components.add(Component.translatable("controllable.gui.clear_binding", InputConstants.Type.MOUSE.getOrCreate(1).getDisplayName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
        }
        return components;
    }

    public interface ButtonOnPress
    {
        boolean onPress(int button);
    }
}
