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
public class SelectKeyBindingScreen extends KeyBindingListMenuScreen
{
    //private final Component note; //TODO reimplement note into list menu
    private Button resetButton;

    public SelectKeyBindingScreen(Screen parent)
    {
        super(parent, new TranslatableComponent("controllable.gui.title.select_key_bindings"), 22);
        this.setSubTitle(new TranslatableComponent("controllable.gui.note").withStyle(ChatFormatting.RED).append(new TranslatableComponent("controllable.gui.key_bind_note").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void init()
    {
        super.init();
        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 32, 150, 20, new TranslatableComponent("controllable.gui.reset"), (button) -> {
            this.minecraft.setScreen(new ConfirmationScreen(this, new TranslatableComponent("controllable.gui.reset_keybinds"), result -> {
                if(result) {
                    List<KeyAdapterBinding> copy = new ArrayList<>(BindingRegistry.getInstance().getKeyAdapters().values());
                    copy.forEach(binding -> {
                        BindingRegistry.getInstance().removeKeyAdapter(binding);
                        RadialMenuHandler.instance().removeBinding(binding);
                    });
                    this.list.children().stream().filter(entry -> entry instanceof KeyBindingItem).map(entry -> (KeyBindingItem) entry).forEach(KeyBindingItem::updateButtons);
                    this.updateButtons();
                }
                return true;
            }));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 32, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.parent);
        }));
        this.updateButtons();
    }

    @Override
    protected void onChange()
    {
        this.updateButtons();
    }

    protected void updateButtons()
    {
        this.resetButton.active = BindingRegistry.getInstance().getKeyAdapters().size() > 0;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        //drawCenteredString(poseStack, this.font, this.note, this.width / 2, 26, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
