package com.mrcrayfish.controllable.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import com.mrcrayfish.framework.api.config.BoolProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class TabOptionToggleItem extends TabOptionBaseItem implements Navigatable
{
    private final AbstractWidget toggle;

    public TabOptionToggleItem(BoolProperty property)
    {
        this(Component.translatable(property.getTranslationKey()), Tooltip.create(Component.literal(property.getComment())), property::get, property::set);
    }

    public TabOptionToggleItem(OptionInstance<Boolean> option)
    {
        this(ClientHelper.getOptionName(option), ClientHelper.getOptionTooltip(option), option::get, value -> {
            option.set(value);
            Minecraft.getInstance().options.save();
        });
    }

    public TabOptionToggleItem(Component label, Tooltip tooltip, Supplier<Boolean> getter, Consumer<Boolean> setter)
    {
        super(label);
        this.toggle = CycleButton.onOffBuilder(getter.get())
                .withTooltip(value -> tooltip)
                .withInitialValue(getter.get())
                .displayOnlyValue()
                .create(0, 0, 100, 20, CommonComponents.EMPTY, (button, value) -> {
                    setter.accept(value);
                });
        this.toggle.setTooltipDelay(500);
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return ImmutableList.of(this.toggle);
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
        this.toggle.setX(left + listWidth - this.toggle.getWidth() - 20);
        this.toggle.setY(top);
        this.toggle.render(graphics, mouseX, mouseY, partialTick);

        if(Controllable.getInput().isControllerInUse() && ScreenUtil.isMouseWithin(left, top, listWidth, slotHeight, mouseX, mouseY))
        {
            ClientHelper.drawButton(graphics, left + listWidth - 16, top + (slotHeight - 11) / 2, Buttons.A);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(!Controllable.getInput().isControllerInUse())
            return super.mouseClicked(mouseX, mouseY, button);

        if(button != GLFW.GLFW_MOUSE_BUTTON_1)
            return false;

        if(!this.isMouseOver(mouseX, mouseY))
            return false;

        if(!this.toggle.active || !this.toggle.visible)
            return false;

        this.toggle.playDownSound(Minecraft.getInstance().getSoundManager());
        this.toggle.onClick(mouseX, mouseY);
        return true;
    }
}
