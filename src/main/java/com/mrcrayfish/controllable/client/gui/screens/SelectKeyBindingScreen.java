package com.mrcrayfish.controllable.client.gui.screens;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SelectKeyBindingScreen extends KeyBindingListMenuScreen
{
    private Button resetButton;

    public SelectKeyBindingScreen(Screen parent)
    {
        super(parent, Component.translatable("controllable.gui.title.select_key_bindings"), 22);
        this.setSubTitle(Component.translatable("controllable.gui.note").withStyle(ChatFormatting.RED).append(Component.translatable("controllable.gui.key_bind_note").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void init()
    {
        super.init();
        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 32, 150, 20, Component.translatable("controllable.gui.reset"), (button) -> {
            this.minecraft.setScreen(new ConfirmationScreen(this, Component.translatable("controllable.gui.reset_keybinds"), result -> {
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
}
