package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.EventHelper;
import com.mrcrayfish.controllable.client.util.InputHelper;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

/**
 * Author: MrCrayfish
 */
public class CameraHandler
{
    private static CameraHandler instance;

    private float pitchDelta;
    private float yawDelta;
    private boolean initialized;

    @ApiStatus.Internal
    public CameraHandler()
    {
        Preconditions.checkState(instance == null, "Only one instance of CameraHandler is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            TickEvents.START_CLIENT.register(this::updateRotationDelta);
            TickEvents.START_RENDER.register(this::updateCamera);
            this.initialized = true;
        }
    }

    private void updateRotationDelta()
    {
        this.yawDelta = 0;
        this.pitchDelta = 0;

        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null || mc.player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        float thumbstickX = InputHelper.getCombinedPressedValue(controller, ButtonBindings.LOOK_LEFT, ButtonBindings.LOOK_RIGHT);
        float thumbstickY = InputHelper.getCombinedPressedValue(controller, ButtonBindings.LOOK_UP, ButtonBindings.LOOK_DOWN);
        if(thumbstickX * thumbstickX > 0 || thumbstickY * thumbstickY > 0)
        {
            float pitchSensitivity = Config.CLIENT.options.pitchSensitivity.get().floatValue();
            float yawSensitivity = Config.CLIENT.options.yawSensitivity.get().floatValue();
            float rotationSpeed = Config.CLIENT.options.rotationSpeed.get().floatValue();
            float spyglassSensitivity = mc.player.isScoping() ? Config.CLIENT.options.spyglassSensitivity.get().floatValue() : 1.0F;

            Value<Float> yawSpeed = new Value<>(rotationSpeed * yawSensitivity * spyglassSensitivity);
            Value<Float> pitchSpeed = new Value<>(rotationSpeed * pitchSensitivity * spyglassSensitivity);
            if(!EventHelper.postUpdateCameraEvent(yawSpeed, pitchSpeed))
            {
                if(thumbstickX * thumbstickX > 0)
                {
                    this.yawDelta = yawSpeed.get() * thumbstickX;
                }
                if(thumbstickY * thumbstickY > 0)
                {
                    this.pitchDelta = pitchSpeed.get() * thumbstickY;
                }
            }

            // Mark the controller as in use because the camera is turning
            controller.updateInputTime();
        }
    }

    private void updateCamera(DeltaTracker tracker)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.screen != null)
            return;

        // Don't turn if nothing to change
        if(this.yawDelta == 0 && this.pitchDelta == 0)
            return;

        float elapsedTicks = tracker.getGameTimeDeltaTicks();
        if(!Controllable.getRadialMenu().isVisible())
        {
            // Turn the camera based on how much time has elapsed during the frame. Turning the camera
            // at a fixed rate per frame would mean that higher frame rates would turn the camera
            // faster. By multiplying with elapsedTicks, we only rotate the camera required for each
            // frame; this keeps the rotation speed consistent over time regardless of frame rate.
            double elapsedDeltaYaw = this.yawDelta * elapsedTicks;
            double elapsedDeltaPitch = this.pitchDelta * elapsedTicks;

            // If enabled by the user, invert the yaw and/or pitch for more preferable controls
            elapsedDeltaYaw *= (Config.CLIENT.options.invertRotation.get() ? -1 : 1);
            elapsedDeltaPitch *= (Config.CLIENT.options.invertLook.get() ? -1 : 1);

            // Finally turn the camera by turning the player
            mc.player.turn(elapsedDeltaYaw, elapsedDeltaPitch);
        }
    }
}
