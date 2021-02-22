package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.client.settings.ControllableOptionSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class OptionSliderWidget extends GuiButton
{
    private float sliderValue;
    public boolean dragging;
    private ControllableOptionSlider option;

    public OptionSliderWidget(int buttonId, int x, int y, int width, ControllableOptionSlider option)
    {
        super(buttonId, x, y, width, 20, "");
        this.sliderValue = 1.0F;
        this.option = option;
        this.sliderValue = option.normalize();
        this.displayString = option.getFormatter().apply(option.getGetter().get());
    }

    @Override
    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if(this.visible)
        {
            if(this.dragging)
            {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
                this.option.setValue(this.sliderValue);
                this.sliderValue = this.option.normalize();
                this.displayString = this.option.getFormatter().apply(this.option.getGetter().get());
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if(super.mousePressed(mc, mouseX, mouseY))
        {
            this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            this.option.setValue(this.sliderValue);
            this.displayString = this.option.getFormatter().apply(this.option.getGetter().get());
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY)
    {
        this.dragging = false;
    }
}
