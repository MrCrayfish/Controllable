package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
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
    //private static float prevHealth = -1;

    public static void init()
    {
        //TickEvents.START_CLIENT.register(ControllerEvents::onClientTick);
        ScreenEvents.INIT.register(ControllerEvents::onScreenInit);
        ScreenEvents.MODIFY_WIDGETS.register(ControllerEvents::onModifyScreenWidgets);
    }

    /*@SubscribeEvent(receiveCanceled = true)
    public void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        if(event.getEntity() != Minecraft.getInstance().player)
        {
            return;
        }

        if(!Config.CLIENT.client.options.forceFeedback.get())
        {
            return;
        }

        Stops vibration from running because controller is not in use
        if(Controllable.getInput().getLastUse() <= 0)
        {
            return;
        }

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            UseAnim action = event.getItem().getUseAnimation();
            switch(action)
            {
                case BLOCK -> magnitudeFactor = 0.25F;
                case SPEAR -> magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 0.25F) / 0.25F;
                case BOW ->  magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.0F);
                case CROSSBOW -> magnitudeFactor = Mth.clamp((event.getItem().getUseDuration() - event.getDuration()) / 20F, 0.0F, 1.5F) / 1.5F;
            }
            //controller.getGamepadState().rumble(0.5F * magnitudeFactor, 0.5F * magnitudeFactor, 50); //50ms is one tick
        }
    }

    private static void onClientTick()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && Config.CLIENT.client.options.forceFeedback.get())
        {
            if(prevHealth == -1)
            {
                prevHealth = mc.player.getHealth();
            }
            else if(prevHealth > mc.player.getHealth())
            {
                float difference = prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                //controller.getGamepadState().rumble(1.0F, 1.0F, (int) (800 * magnitude));
                prevHealth = mc.player.getHealth();
            }
            else
            {
                prevHealth = mc.player.getHealth();
            }
        }
        else if(prevHealth != -1)
        {
            prevHealth = -1;
        }
    }*/

    private static void onScreenInit(Screen screen)
    {
        ButtonBinding.resetButtonStates();
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
