package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.settings.ButtonIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingButton extends Button
{
    private final ButtonBinding binding;
    private final ButtonOnPress onPress;
    private static final int MAX_DISPLAYED_BUTTONS = 3; // Maximum buttons to show as icons

    public ButtonBindingButton(int x, int y, ButtonBinding binding, ButtonOnPress onPress)
    {
        super(x, y, 40, 20, CommonComponents.EMPTY, btn -> {}, DEFAULT_NARRATION);
        this.binding = binding;
        this.onPress = onPress;
    }

    public ButtonBinding getBinding()
    {
        return this.binding;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        
        if(this.binding.isUnbound())
            return;
            
        if(this.binding.isMultiButton())
        {
            // Get all buttons in the binding
            List<Integer> buttons = new ArrayList<>(this.binding.getButtons());
            int buttonCount = buttons.size();
            int displayCount = Math.min(buttonCount, MAX_DISPLAYED_BUTTONS);
            
            int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * 13;
            int size = 13;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            
            // Calculate total width needed for all icons + plus signs
            int iconWidth = size;
            int plusWidth = 4; // Width of "+" character
            int spacing = 2;
            int totalWidth = (iconWidth * displayCount) + (plusWidth * (displayCount - 1)) + (spacing * (displayCount - 1));
            
            // Starting X position to center everything
            int startX = this.getX() + (this.width - totalWidth) / 2;
            int currentX = startX;
            
            // Draw each button icon with "+" between them
            for(int i = 0; i < displayCount; i++)
            {
                int button = buttons.get(i);
                int texU = button * 13;
                
                // Draw button icon
                graphics.blit(ButtonIcons.TEXTURE, currentX, this.getY() + 3, texU, texV, size, size, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);
                currentX += iconWidth + spacing;
                
                // Draw "+" between icons (but not after the last one)
                if(i < displayCount - 1)
                {
                    graphics.drawString(Minecraft.getInstance().font, "+", currentX, this.getY() + 6, 0xFFFFFFFF, false);
                    currentX += plusWidth + spacing;
                }
            }
            
            // If there are more buttons than we can display, show "+N" at the end
            if(buttonCount > MAX_DISPLAYED_BUTTONS)
            {
                int remaining = buttonCount - MAX_DISPLAYED_BUTTONS;
                graphics.drawString(Minecraft.getInstance().font, "+" + remaining, currentX + 2, this.getY() + 6, 0xAAAAAAAA, false);
            }
        }
        else
        {
            // Single button binding - original behavior
            int texU = this.binding.getButton() * 13;
            int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * 13;
            int size = 13;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(ButtonIcons.TEXTURE, this.getX() + (this.width - size) / 2 + 1, this.getY() + 3, texU, texV, size, size, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);
        }
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

    public interface ButtonOnPress
    {
        boolean onPress(int button);
    }
}