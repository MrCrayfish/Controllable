package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SelectKeyBindingScreen extends Screen
{
    private Screen parent;
    private KeyBindingList bindingList;
    private final Component note;
    private Button resetButton;

    public SelectKeyBindingScreen(Screen parent)
    {
        super(new TranslatableComponent("controllable.gui.title.select_key_bindings"));
        this.note = new TranslatableComponent("controllable.gui.note").withStyle(ChatFormatting.RED).append(new TranslatableComponent("controllable.gui.key_bind_note").withStyle(ChatFormatting.GRAY));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        this.bindingList = new KeyBindingList(this, this.minecraft, this.width + 10, this.height, 45, this.height - 44, 22);
        this.addWidget(this.bindingList);
        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controllable.gui.reset"), (button) -> {
            List<KeyAdapterBinding> copy = new ArrayList<>(BindingRegistry.getInstance().getKeyAdapters().values());
            copy.forEach(binding -> {
                BindingRegistry.getInstance().removeKeyAdapter(binding);
                RadialMenuHandler.instance().removeBinding(binding);
            });
            this.bindingList.children().stream().filter(entry -> entry instanceof KeyBindingList.KeyBindingEntry).map(entry -> (KeyBindingList.KeyBindingEntry) entry).forEach(KeyBindingList.KeyBindingEntry::updateButtons);
            this.updateButtons();
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.parent);
        }));
        this.updateButtons();
    }

    void updateButtons()
    {
        this.resetButton.active = BindingRegistry.getInstance().getKeyAdapters().size() > 0;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        this.bindingList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, this.note, this.width / 2, 26, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
