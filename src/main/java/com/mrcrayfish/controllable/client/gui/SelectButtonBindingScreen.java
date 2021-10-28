package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Author: MrCrayfish
 */
public class SelectButtonBindingScreen extends Screen
{
    private RadialMenuConfigureScreen parentScreen;
    private ButtonBindingList bindingList;

    public SelectButtonBindingScreen(RadialMenuConfigureScreen parentScreen)
    {
        super(new TranslatableComponent("controllable.gui.title.select_button_bindings"));
        this.parentScreen = parentScreen;
    }

    public RadialMenuConfigureScreen getRadialConfigureScreen()
    {
        return this.parentScreen;
    }

    @Override
    protected void init()
    {
        this.bindingList = new ButtonBindingList(this, this.minecraft, this.width + 10, this.height, 45, this.height - 44, 20);
        this.addWidget(this.bindingList);
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controllable.gui.restoreDefaults"), (button) -> {
            this.parentScreen.getBindings().clear();
            this.parentScreen.getBindings().addAll(RadialMenuHandler.instance().getDefaults());
            this.bindingList.children().stream().filter(entry -> entry instanceof ButtonBindingList.BindingEntry).map(entry -> (ButtonBindingList.BindingEntry) entry).forEach(ButtonBindingList.BindingEntry::updateButtons);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.parentScreen);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        this.bindingList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
