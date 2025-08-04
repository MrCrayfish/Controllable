package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.gui.widget.LazySlider;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.framework.api.config.DoubleProperty;
import com.mrcrayfish.framework.api.config.validate.NumberRange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.time.Duration;
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
        if(!(property.getValidator() instanceof NumberRange<Double>(Double minValue, Double maxValue)))
            throw new IllegalArgumentException("Double property must have a number range");
        this.slider = new LazySlider(0, 0, 100, 20, this.label, property.get(), minValue, maxValue, stepSize, property::set);
        this.slider.setTooltip(createTooltipWithWidth(createTooltipMessage(property), 250)); // TODO trim valid values and whitespace
        this.slider.setTooltipDelay(Duration.ofMillis(500));
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
    public void render(GuiGraphics graphics, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean hovered, float partialTick)
    {
        super.render(graphics, slotIndex, top, left, listWidth, slotHeight, mouseX, mouseY, hovered, partialTick);
        this.slider.active = this.isOptionActive();
        this.slider.setX(left + listWidth - this.slider.getWidth() - 20);
        this.slider.setY(top);
        this.slider.render(graphics, mouseX, mouseY, partialTick);

        Controller controller = Controllable.getController();
        if(this.slider.active && controller != null && controller.isBeingUsed() && ScreenHelper.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ClientHelper.drawButton(graphics, left + listWidth - this.slider.getWidth() - 20 - 17, top + (slotHeight - 11) / 2, ButtonBindings.NEXT_CREATIVE_TAB.getButton());
            ClientHelper.drawButton(graphics, left + listWidth - 16, top + (slotHeight - 11) / 2, ButtonBindings.PREVIOUS_CREATIVE_TAB.getButton());

            long currentTime = System.currentTimeMillis();
            if(currentTime - this.lastChange > 100)
            {
                boolean changing = false;
                if(ButtonBindings.NEXT_CREATIVE_TAB.isButtonDown())
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_STEP, 0.7F, 0.25F));
                    this.slider.stepBackward();
                    this.lastChange = currentTime;
                    this.save = true;
                    changing = true;
                }
                else if(ButtonBindings.PREVIOUS_CREATIVE_TAB.isButtonDown())
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

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return this.slider.mouseReleased(mouseX, mouseY, button);
    }
}
