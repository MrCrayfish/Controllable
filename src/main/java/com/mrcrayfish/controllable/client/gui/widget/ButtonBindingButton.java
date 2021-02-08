package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.RenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingButton extends Button
{
    private ButtonBinding binding;

    public ButtonBindingButton(int x, int y, ButtonBinding binding, IPressable onPress)
    {
        super(x, y, 20, 20, StringTextComponent.EMPTY, onPress);
        this.binding = binding;
    }

    public ButtonBinding getBinding()
    {
        return this.binding;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        int texU = this.binding.getButton() * 13;
        int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * 13;
        int size = 13;
        Minecraft.getInstance().getTextureManager().bindTexture(RenderEvents.CONTROLLER_BUTTONS);
        this.blit(matrixStack, this.x + (this.width - size) / 2 + 1, this.y + 3, texU, texV, size, size);
    }
}
