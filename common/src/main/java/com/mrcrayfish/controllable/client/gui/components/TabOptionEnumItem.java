package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.SettingEnum;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.framework.api.config.EnumProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class TabOptionEnumItem<T extends Enum<T> & SettingEnum> extends TabOptionBaseItem implements Navigatable
{
    private final CycleButton<T> cycle;
    private boolean canChange;

    public TabOptionEnumItem(EnumProperty<T> property)
    {
        this(Component.translatable(property.getTranslationKey()), createTooltipMessage(property), property::get, property::set);
    }

    @SuppressWarnings("unchecked")
    public TabOptionEnumItem(Component label, Component tooltip, Supplier<T> getter, Consumer<T> setter)
    {
        super(label);
        List<T> values = (List<T>) Arrays.asList(getter.get().getClass().getEnumConstants());
        this.cycle = CycleButton.builder(T::getLabel)
                .withValues(values)
                .withInitialValue(getter.get())
                .withTooltip(value -> createTooltipWithWidth(tooltip, 250))
                .displayOnlyValue()
                .create(0, 0, 100, 20, this.label, (button, value) -> {
                    setter.accept(value);
                });
        this.cycle.setTooltipDelay(Duration.ofMillis(500));
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return ImmutableList.of(this.cycle);
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
        this.cycle.active = this.isOptionActive();
        this.cycle.setX(left + listWidth - this.cycle.getWidth() - 20);
        this.cycle.setY(top);
        this.cycle.render(graphics, mouseX, mouseY, partialTick);

        Controller controller = Controllable.getController();
        if(this.cycle.active && controller != null && controller.isBeingUsed() && ScreenHelper.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ClientHelper.drawButton(graphics, left + listWidth - this.cycle.getWidth() - 20 - 17, top + (slotHeight - 11) / 2, ButtonBindings.NEXT_CREATIVE_TAB.getButton());
            ClientHelper.drawButton(graphics, left + listWidth - 16, top + (slotHeight - 11) / 2, ButtonBindings.PREVIOUS_CREATIVE_TAB.getButton());

            if(ButtonBindings.NEXT_CREATIVE_TAB.isButtonDown())
            {
                if(this.canChange)
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 0.25F));
                    this.cycle.mouseScrolled(0, 0, 0, 1);
                    this.canChange = false;
                }
            }
            else if(ButtonBindings.PREVIOUS_CREATIVE_TAB.isButtonDown())
            {
                if(this.canChange)
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 0.75F, 0.25F));
                    this.cycle.mouseScrolled(0, 0, 0, -1);
                    this.canChange = false;
                }
            }
            else if(!this.canChange)
            {
                this.canChange = true;
            }
        }
    }
}
