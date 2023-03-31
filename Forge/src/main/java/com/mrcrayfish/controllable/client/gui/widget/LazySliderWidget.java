package com.mrcrayfish.controllable.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A custom slider that only sends change updates when the mouse is released
 *
 * Author: MrCrayfish
 */
public class LazySliderWidget extends ForgeSlider
{
    private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");

    private final Consumer<Double> onChange;
    private boolean pressed = false;

    public LazySliderWidget(Component label, int x, int y, int width, int height, double minValue, double maxValue, double currentValue, double stepSize, Consumer<Double> onChange)
    {
        super(x, y, width, height, label, CommonComponents.EMPTY, minValue, maxValue, currentValue, stepSize, 1, true);
        this.onChange = onChange;
    }

    @Override
    protected void updateMessage()
    {
        this.setMessage(Component.empty().append(this.prefix).append(": ").append(this.getValueString()));
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        this.pressed = true;
    }

    // Only send change when releasing mouse to avoid lots of calls to save config
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.isValidClickButton(button) && this.pressed)
        {
            this.onChange.accept(this.getValue());
            this.pressed = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Draw background
        blitNineSliced(poseStack, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 0);

        // Draw slider
        int sliderYImage = (this.isHoveredOrFocused() ? 3 : 2) * 20;
        int sliderX = this.getX() + (int) (this.value * (double) (this.width - 8));
        blitNineSliced(poseStack, sliderX, this.getY(), 8, 20, 20, 4, 200, 20, 0, sliderYImage);

        // Draw text
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int textColor = (this.active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(this.alpha * 255.0F) << 24;
        this.renderScrollingString(poseStack, Minecraft.getInstance().font, 2, textColor);
    }
}
