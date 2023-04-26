package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import java.util.List;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    private static float prevHealth = -1;

    public static void init()
    {
        TickEvents.START_CLIENT.register(ControllerEvents::onClientTick);
        ScreenEvents.INIT.register(ControllerEvents::onScreenInit);
        ScreenEvents.MODIFY_WIDGETS.register(ControllerEvents::onModifyScreenWidgets);
    }

    public static void onPlayerUsingItem(Entity entity, ItemStack stack, int duration)
    {
        if(entity != Minecraft.getInstance().player)
            return;

        if(!Config.CLIENT.client.options.rumble.get())
            return;

        if(Controllable.getInput().getLastUse() <= 0)
            return;

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            UseAnim action = stack.getUseAnimation();
            switch(action)
            {
                case BLOCK -> magnitudeFactor = 0.25F;
                case SPEAR -> magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 0.25F) / 0.25F;
                case BOW ->  magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 1.0F);
                case CROSSBOW -> magnitudeFactor = Mth.clamp((stack.getUseDuration() - duration) / 20F, 0.0F, 1.5F) / 1.5F;
                case EAT -> magnitudeFactor = 0.15F;
            }
            controller.rumble(0.15F * magnitudeFactor, 0.5F * magnitudeFactor, 70); //50ms is one tick
        }
    }

    private static void onClientTick()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && Config.CLIENT.client.options.rumble.get())
        {
            if(prevHealth == -1 || mc.player.getHealth() > prevHealth)
            {
                prevHealth = mc.player.getHealth();
            }
            else if(prevHealth > mc.player.getHealth())
            {
                float difference = prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                controller.rumble(1.0F, 1.0F, (int) (800 * magnitude));
                prevHealth = mc.player.getHealth();
            }
        }
        else if(prevHealth != -1)
        {
            prevHealth = -1;
        }
    }

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
