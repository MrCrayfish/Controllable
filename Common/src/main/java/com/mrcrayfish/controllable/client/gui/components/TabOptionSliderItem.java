package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.gui.widget.LazySlider;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import com.mrcrayfish.framework.api.config.DoubleProperty;
import com.mrcrayfish.framework.api.config.validate.NumberRange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TabOptionSliderItem extends TabOptionBaseItem implements Navigatable
{
    private final LazySlider slider;
    private long lastChange;
    private boolean save;

    public TabOptionSliderItem(DoubleProperty property, double stepSize)
    {
        super(Component.translatable(property.getTranslationKey()));
        if(!(property.getValidator() instanceof NumberRange<Double> range))
            throw new IllegalArgumentException("Double property must have a number range");
        this.slider = new LazySlider(0, 0, 100, 20, this.label, property.get(), range.minValue(), range.maxValue(), stepSize, property::set);
        this.slider.setTooltip(Tooltip.create(Component.literal(property.getComment())));
        this.slider.setTooltipDelay(500);
        this.slider.valueOnly();
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return ImmutableList.of(this.slider);
    }

    @Override
    public List<GuiEventListener> elements()
    {
        return Collections.emptyList();
    }

    @Override
    public void render(PoseStack poseStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTick)
    {
        super.render(poseStack, slotIndex, top, left, listWidth, slotHeight, mouseX, mouseY, hovered, partialTick);
        this.slider.setX(left + listWidth - this.slider.getWidth() - 20);
        this.slider.setY(top);
        this.slider.render(poseStack, mouseX, mouseY, partialTick);

        if(Controllable.getInput().isControllerInUse() && ScreenUtil.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ClientHelper.drawButton(poseStack, left + listWidth - this.slider.getWidth() - 20 - 17, top + (slotHeight - 11) / 2, Buttons.LEFT_TRIGGER);
            ClientHelper.drawButton(poseStack, left + listWidth - 16, top + (slotHeight - 11) / 2, Buttons.RIGHT_TRIGGER);

            long currentTime = System.currentTimeMillis();
            if(currentTime - this.lastChange > 100)
            {
                boolean changing = false;
                if(ButtonBindings.NEXT_RECIPE_TAB.isButtonDown())
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_STEP, 0.7F, 0.25F));
                    this.slider.stepBackward();
                    this.lastChange = currentTime;
                    this.save = true;
                    changing = true;
                }
                else if(ButtonBindings.PREVIOUS_RECIPE_TAB.isButtonDown())
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_STEP, 0.75F, 0.25F));
                    this.slider.stepForward();
                    this.lastChange = currentTime;
                    this.save = true;
                    changing = true;
                }
                if(!changing && this.save)
                {
                    this.slider.triggerChangeCallback();
                    this.save = false;
                }
            }
        }
    }
}
