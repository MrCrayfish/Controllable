package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.AnalogMovement;
import com.mrcrayfish.controllable.client.settings.Thumbstick;
import com.mrcrayfish.controllable.client.util.EventHelper;
import com.mrcrayfish.framework.api.event.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.ApiStatus;

/**
 * Author: MrCrayfish
 */
public class MovementHandler
{
    private static MovementHandler instance;

    private boolean initialized;

    @ApiStatus.Internal
    public MovementHandler()
    {
        Preconditions.checkState(instance == null, "Only one instance of MovementHandler is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            ClientEvents.PLAYER_INPUT_UPDATE.register(this::updateInput);
            this.initialized = true;
        }
    }

    private void updateInput(Player player, Input input)
    {
        LocalPlayer localPlayer = (LocalPlayer) player;
        if(localPlayer == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();

        if(!mc.options.toggleCrouch().get())
        {
            if(ButtonBindings.SNEAK.isButtonDown())
            {
                input.shiftKeyDown = true;
                controller.updateInputTime();
            }
        }

        if(mc.screen == null)
        {
            if((!Controllable.getRadialMenu().isVisible() || Config.CLIENT.options.radialThumbstick.get() != Thumbstick.LEFT) && !EventHelper.postMoveEvent())
            {
                float sneakSpeed = (float) localPlayer.getAttributeValue(Attributes.SNEAKING_SPEED);
                float sneakBonus = localPlayer.isMovingSlowly() ? sneakSpeed : 1.0F;
                float inputX = controller.getLThumbStickXValue();
                float inputY = controller.getLThumbStickYValue();

                AnalogMovement movement = Config.CLIENT.options.analogMovement.get();
                if(movement != AnalogMovement.ALWAYS)
                {
                    ServerData data = mc.getCurrentServer();
                    if(movement != AnalogMovement.LOCAL_ONLY || data != null && data.type() == ServerData.Type.OTHER)
                    {
                        inputX = Math.abs(inputX) >= 0.5F ? Math.signum(inputX) : 0;
                        inputY = Math.abs(inputY) >= 0.5F ? Math.signum(inputY) : 0;
                    }
                }

                if(Math.abs(inputY) > 0)
                {
                    input.up = inputY < 0;
                    input.down = inputY > 0;
                    input.forwardImpulse = -inputY;
                    input.forwardImpulse *= sneakBonus;
                    controller.updateInputTime();
                }

                float threshold = localPlayer.getVehicle() instanceof Boat ? 0.5F : 0;
                if(Math.abs(inputX) > threshold)
                {
                    input.right = inputX > 0;
                    input.left = inputX < 0;
                    input.leftImpulse = -inputX;
                    input.leftImpulse *= sneakBonus;
                    controller.updateInputTime();
                }
            }

            if(ButtonBindings.JUMP.isButtonDown())
            {
                input.jumping = true;
                controller.updateInputTime();
            }
        }
    }
}
