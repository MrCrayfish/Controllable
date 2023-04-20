package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    private float prevHealth = -1;

    @SubscribeEvent(receiveCanceled = true)
    public void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        Entity entity = event.getEntity();
        if(entity != Minecraft.getInstance().player)
            return;

        if(!Config.CLIENT.options.rumble.get())
            return;

        if(Controllable.getInput().getLastUse() <= 0)
            return;

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            float magnitudeFactor = 0.5F;
            ItemStack stack = event.getItem();
            int duration = event.getDuration();
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && Config.CLIENT.options.rumble.get())
        {
            if(this.prevHealth == -1 || mc.player.getHealth() > this.prevHealth)
            {
                this.prevHealth = mc.player.getHealth();
            }
            else if(this.prevHealth > mc.player.getHealth())
            {
                float difference = this.prevHealth - mc.player.getHealth();
                float magnitude = difference / mc.player.getMaxHealth();
                controller.rumble(1.0F, 1.0F, (int) (800 * magnitude));
                this.prevHealth = mc.player.getHealth();
            }
        }
        else if(this.prevHealth != -1)
        {
            this.prevHealth = -1;
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init event)
    {
        ButtonBinding.resetButtonStates();
    }

    @SubscribeEvent
    public void onModifyScreenWidgets(ScreenEvent.Init event)
    {
        Screen screen = event.getScreen();
        if(screen instanceof OptionsScreen)
        {
            int y = screen.height / 6 + 72 - 6;
            event.addListener(new ControllerButton((screen.width / 2) + 5 + 150 + 4, y, button -> {
                Minecraft.getInstance().setScreen(new SettingsScreen(screen));
            }));
        }
    }
}
