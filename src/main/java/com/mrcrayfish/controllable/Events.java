package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.event.ControllerInputEvent;
import com.mrcrayfish.controllable.event.ControllerMoveEvent;
import com.mrcrayfish.controllable.event.ControllerTurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class Events
{
    @SubscribeEvent
    public static void onRender(TickEvent.RenderTickEvent event)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        while(Controllers.next())
        {
            if(Controllers.isEventButton() && Controllers.getEventSource() == controller)
            {
                int button = Controllers.getEventControlIndex();
                boolean state = Controllers.getEventButtonState();
                if(!MinecraftForge.EVENT_BUS.post(new ControllerInputEvent(button, state)))
                {
                    handleMinecraftInput(button, state);
                }
            }
        }

        if(event.phase == TickEvent.Phase.END)
            return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null)
            return;

        if(!MinecraftForge.EVENT_BUS.post(new ControllerTurnEvent()))
        {
            /* Handles rotating the yaw of player */
            if(controller.getZAxisValue() != 0.0F || controller.getRZAxisValue() != 0.0F)
            {
                float rotationYaw = 10.0F * (controller.getZAxisValue() > 0.0F ? 1 : -1) * Math.abs(controller.getZAxisValue());
                float rotationPitch = 7.5F * (controller.getRZAxisValue() > 0.0F ? 1 : -1) * Math.abs(controller.getRZAxisValue());
                player.turn(rotationYaw, -rotationPitch);
            }
        }
    }

    @SubscribeEvent
    public static void onInputUpdate(InputUpdateEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(!MinecraftForge.EVENT_BUS.post(new ControllerMoveEvent()))
        {
            if(Minecraft.getMinecraft().currentScreen == null)
            {
                if(controller.getYAxisValue() != 0.0F)
                {
                    int dir = controller.getYAxisValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * Math.abs(controller.getYAxisValue());
                }

                if(controller.getXAxisValue() != 0.0F)
                {
                    int dir = controller.getXAxisValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * Math.abs(controller.getXAxisValue());
                }
            }
        }
    }

    private static void handleMinecraftInput(int button, boolean state)
    {
        
    }
}
