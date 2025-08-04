package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    private static boolean released = false;

    public static void init()
    {
        ScreenEvents.INIT.register(ControllerEvents::onScreenInit);
        ScreenEvents.MODIFY_WIDGETS.register(ControllerEvents::onModifyScreenWidgets);
    }

    private static void onScreenInit(Screen screen)
    {
        ButtonBinding.resetButtonStates();

        // Fixes an issue where using item is not stopped after opening a screen
        if(!released)
        {
            released = true;
            Controller controller = Controllable.getController();
            if(controller != null && controller.isBeingUsed())
            {
                Minecraft mc = Minecraft.getInstance();
                if(mc.gameMode != null && mc.player != null && mc.player.isUsingItem())
                {
                    mc.gameMode.releaseUsingItem(mc.player);
                }
            }
        }
        released = false;
    }

    private static void onModifyScreenWidgets(Screen screen, List<AbstractWidget> widgets, Consumer<AbstractWidget> add, Consumer<AbstractWidget> remove)
    {
        if(screen instanceof OptionsScreen)
        {
            Optional<AbstractWidget> btn = widgets.stream().filter(widget -> widget instanceof Button button &&
                button.getMessage().equals(Component.translatable("options.controls"))).findFirst();
            btn.ifPresent(widget -> add.accept(new ControllerButton(widget, button -> {
                Minecraft.getInstance().setScreen(new SettingsScreen(screen));
            })));
        }
    }
}
