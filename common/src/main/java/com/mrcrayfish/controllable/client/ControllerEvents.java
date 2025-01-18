package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.api.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

import java.util.List;
import java.util.Optional;
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
    }

    private static void onModifyScreenWidgets(Screen screen, List<AbstractWidget> widgets, Consumer<AbstractWidget> add, Consumer<AbstractWidget> remove)
    {
        if(screen instanceof OptionsScreen)
        {
            // OptionsScreen doesn't clear widgets on resize, so we have to manage a persistent widget and it's position
            int buttonX = (screen.width / 2) + 5 + 150 + 4;
            int buttonY = 115;
            Optional<AbstractWidget> optional = widgets.stream().filter(widget -> widget instanceof ControllerButton).findFirst();
            optional.ifPresentOrElse(widget -> {
                widget.setPosition(buttonX, buttonY);
            }, () -> {
                add.accept(new ControllerButton(buttonX, buttonY, button -> {
                    Minecraft.getInstance().setScreen(new SettingsScreen(screen));
                }));
            });
        }
    }
}
