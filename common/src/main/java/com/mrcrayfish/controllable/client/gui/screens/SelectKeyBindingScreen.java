package com.mrcrayfish.controllable.client.gui.screens;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.RadialMenu;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class SelectKeyBindingScreen extends KeyBindingListMenuScreen
{
    private static final Component LABEL_NO_KEYBINDS_AVAILABLE = Component.translatable("controllable.gui.no_keybinds_available");

    private final Runnable callback;
    private Button resetButton;

    public SelectKeyBindingScreen(Screen parent, Runnable callback)
    {
        super(parent, Component.translatable("controllable.gui.title.select_key_bindings"), 22);
        this.callback = callback;
        this.setSubTitle(Component.translatable("controllable.gui.note").withStyle(ChatFormatting.RED).append(Component.translatable("controllable.gui.key_bind_note").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void setupFooter(LinearLayout footerLayout)
    {
        super.setupFooter(footerLayout);
        Component resetLabel = ClientHelper.join(Icons.RESET, Component.translatable("controllable.gui.reset"));
        this.resetButton = footerLayout.addChild(Button.builder(resetLabel, button -> {
            Objects.requireNonNull(this.minecraft).setScreen(new ConfirmationScreen(this, Component.translatable("controllable.gui.reset_keybinds"), result -> {
                if(result) {
                    List<KeyAdapterBinding> copy = new ArrayList<>(Controllable.getBindingRegistry().getKeyAdapters().values());
                    copy.forEach(binding -> {
                        Controllable.getBindingRegistry().removeKeyAdapter(binding);
                        Controllable.getRadialMenu().removeBinding(binding);
                    });
                    this.updateButtons();
                    this.rebuildItems();
                }
                return true;
            }));
        }).size(150, 20).build());
        footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.callback.run();
            Objects.requireNonNull(this.minecraft).setScreen(this.parent);
        }).size(150, 20).build());
        this.updateButtons();
    }

    @Override
    protected void repositionElements()
    {
        this.updateButtons();
        super.repositionElements();
    }

    @Override
    protected void onChange()
    {
        this.updateButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        if(this.list.children().isEmpty())
        {
            graphics.drawCenteredString(this.font, LABEL_NO_KEYBINDS_AVAILABLE, this.list.getX() + this.list.getWidth() / 2, this.list.getY() + this.list.getHeight() / 2 - 4, 0xFFFFFF);
        }
    }

    protected void updateButtons()
    {
        this.resetButton.active = !Controllable.getBindingRegistry().getKeyAdapters().isEmpty();
    }
}
