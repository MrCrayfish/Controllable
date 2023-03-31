package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.RenderEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingButton extends Button
{
    private final ButtonBinding binding;

    public ButtonBindingButton(int x, int y, ButtonBinding binding, OnPress onPress)
    {
        super(x, y, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.binding = binding;
    }

    public ButtonBinding getBinding()
    {
        return this.binding;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(poseStack, mouseX, mouseY, partialTicks);
        Controller controller = Controllable.getController();
        if(controller == null || this.binding.getButton() < 0)
            return;
        int texU = this.binding.getButton() * 13;
        int texV = Config.CLIENT.client.options.controllerIcons.get().ordinal() * 13;
        int size = 13;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RenderEvents.CONTROLLER_BUTTONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(poseStack, this.getX() + (this.width - size) / 2 + 1, this.getY() + 3, texU, texV, size, size, RenderEvents.CONTROLLER_BUTTONS_WIDTH, RenderEvents.CONTROLLER_BUTTONS_HEIGHT);
    }
}
