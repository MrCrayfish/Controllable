package com.mrcrayfish.controllable.client.gui;

import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.event.ControllerEvent;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ControllerActionMenu extends ControlsScreen {

    public ControllerActionList.KeyEntry entry;
    public ButtonBinding controllerButtonId;
    public String action;

    private ControllerActionList buttonActionList;
    private Button buttonReset;

    public ControllerActionMenu(Screen screen, GameSettings settings) {
        super(screen, settings);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void init() {
        this.buttonActionList = new ControllerActionList(this, this.minecraft);
        this.children.add(buttonActionList);

        this.buttonReset = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controls.resetAll"), (p_213125_1_) -> {
            Controllable.getButtonRegistry().getButtonBindings().forEach((action, buttonBinding) -> {
                buttonBinding.resetButton();
            });

            try {
                ControllerProperties.saveMappings();
            } catch (ConfigLoadException e) {
                e.printStackTrace();
            }

        }));
        this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done"), (p_213124_1_) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
            MinecraftForge.EVENT_BUS.unregister(this); // Prevent storing this instance in the event bus.
        }));
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground();
        this.buttonActionList.render(p_render_1_, p_render_2_, p_render_3_);
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 8, 16777215);
        boolean flag = false;

        for (ButtonBinding buttonBinding : Controllable.getButtonRegistry().getButtonBindings().values()) {
            if (!buttonBinding.isDefault()) {
                flag = true;
                break;
            }
        }

        this.buttonReset.active = flag;

        for (Widget button : this.buttons) {
            button.render(p_render_1_, p_render_2_, p_render_3_);
        }
    }


    @SubscribeEvent
    public void buttonPressedEvent(ControllerEvent.ButtonInput e) {
        if (Minecraft.getInstance().currentScreen instanceof ControllerActionMenu) {
            if (this.controllerButtonId != null) {

                if (e.getState()) {
                    Controllable.getButtonRegistry().getButton(action).setButton(e.getModifiedButton());

                    try {
                        ControllerProperties.saveMappings();
                    } catch (ConfigLoadException ex) {
                        ex.printStackTrace();
                    }

                    controllerButtonId = null;
                    entry = null;
                    action = null;
                }

                this.time = Util.milliTime();
            }
        }
    }
}
