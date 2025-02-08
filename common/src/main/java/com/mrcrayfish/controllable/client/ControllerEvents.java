package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    public static void init()
    {
        ScreenEvents.INIT.register(ControllerEvents::onScreenInit);
        ScreenEvents.MODIFY_WIDGETS.register(ControllerEvents::onModifyScreenWidgets);
    }

    private static void onScreenInit(Screen screen)
    {
        ButtonBinding.resetButtonStates();

        // Fixes an issue where using item is not stopped after opening a screen
        Controller controller = Controllable.getController();
        if(controller != null && Controllable.getInput().isControllerInUse())
        {
            Minecraft mc = Minecraft.getInstance();
            if(mc.gameMode != null && mc.player != null && mc.player.isUsingItem())
            {
                mc.gameMode.releaseUsingItem(mc.player);
            }
        }
    }

    private static void onModifyScreenWidgets(Screen screen, List<AbstractWidget> widgets, Consumer<AbstractWidget> add, Consumer<AbstractWidget> remove)
    {
        if(screen instanceof OptionsScreen)
        {
            int y = screen.height / 6 + 72 - 6;
            add.accept(new ControllerButton((screen.width / 2) + 5 + 150 + 4, y, button -> {
                Minecraft.getInstance().setScreen(new SettingsScreen(screen));
            }));
        }
    }
}
