package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.ButtonBindingData;
import com.mrcrayfish.controllable.client.gui.RadialItemList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class RadialMenuConfigureScreen extends Screen
{
    private List<ButtonBindingData> bindings;
    private RadialItemList list;

    public RadialMenuConfigureScreen(LinkedHashSet<ButtonBindingData> bindings)
    {
        super(Component.translatable("controllable.gui.title.radial_menu_configure"));
        this.bindings = new ArrayList<>(bindings);
    }

    @Override
    protected void init()
    {
        this.list = new RadialItemList(this.minecraft, this.width, this.height, 45, this.height - 44, this.bindings);
        this.addWidget(this.list);
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 100, 20, CommonComponents.GUI_DONE, buttons -> {
            RadialMenuHandler.instance().setBindings(new LinkedHashSet<>(this.bindings));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 29, 100, 20, Component.translatable("controllable.gui.add_binding"), buttons -> {
            Objects.requireNonNull(this.minecraft).setScreen(new SelectButtonBindingScreen(this));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 55, this.height - 29, 100, 20, CommonComponents.GUI_CANCEL, buttons -> {
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public List<ButtonBindingData> getBindings()
    {
        return this.bindings;
    }
}
