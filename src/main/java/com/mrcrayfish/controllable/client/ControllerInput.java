package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.settings.ControllerOptions;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.registry.ButtonRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_UP;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerInput
{
    private static final ResourceLocation CURSOR_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/cursor.png");

    private int lastUse = 0;
    private boolean keyboardSneaking = false;
    private boolean keyboardSprinting = false;

    private boolean sneaking = false;

    private boolean sprinting = false;

    private boolean isFlying = false;
    private boolean nearSlot = false;
    private double virtualMouseX;
    private double virtualMouseY;
    private float prevXAxis;
    private float prevYAxis;
    private int prevTargetMouseX;
    private int prevTargetMouseY;
    private int targetMouseX;
    private int targetMouseY;
    private double mouseSpeedX;
    private double mouseSpeedY;
    private boolean moved;
    private float targetPitch;
    private float targetYaw;

    private Entity aimAssistTarget;
    private boolean aimAssistIgnore = true; //If true, aim assist will not aim at an entity until it becomes false. True when mouse is moved, false when controller is moved. Mouse will always override controller state.
    private double rawMouseX;
    private double rawMouseY;

    private int currentAttackTimer;

    private int dropCounter = -1;
    private boolean mouseMoved;
    private boolean controllerInput;

    public double getVirtualMouseX()
    {
        return virtualMouseX;
    }

    public double getVirtualMouseY()
    {
        return virtualMouseY;
    }

    public int getLastUse()
    {
        return lastUse;
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            prevTargetMouseX = targetMouseX;
            prevTargetMouseY = targetMouseY;

            if(lastUse > 0)
            {
                lastUse--;
            }

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            if(Math.abs(controller.getLTriggerValue()) >= 0.1F || Math.abs(controller.getRTriggerValue()) >= 0.1F)
            {
                lastUse = 100;
            }

            Minecraft mc = Minecraft.getInstance();
            if(mc.mouseHelper.isMouseGrabbed())
                return;

            if(mc.currentScreen == null || mc.currentScreen instanceof ControllerLayoutScreen)
                return;

            float deadZone = (float) Controllable.getOptions().getDeadZone();

            /* Only need to run code if left thumb stick has input */
            boolean moving = Math.abs(controller.getLThumbStickXValue()) >= deadZone || Math.abs(controller.getLThumbStickYValue()) >= deadZone;
            if(moving)
            {
                /* Updates the target mouse position when the initial thumb stick movement is
                 * detected. This fixes an issue when the user moves the cursor with the mouse then
                 * switching back to controller, the cursor would jump to old target mouse position. */
                if(Math.abs(prevXAxis) < deadZone && Math.abs(prevYAxis) < deadZone)
                {
                    double mouseX = mc.mouseHelper.getMouseX();
                    double mouseY = mc.mouseHelper.getMouseY();
                    if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse())
                    {
                        mouseX = virtualMouseX;
                        mouseY = virtualMouseY;
                    }
                    prevTargetMouseX = targetMouseX = (int) mouseX;
                    prevTargetMouseY = targetMouseY = (int) mouseY;
                }

                float xAxis = (controller.getLThumbStickXValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickXValue());
                if(Math.abs(xAxis) >= deadZone)
                {
                    mouseSpeedX = xAxis;
                }
                else
                {
                    mouseSpeedX = 0.0F;
                }

                float yAxis = (controller.getLThumbStickYValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickYValue());
                if(Math.abs(yAxis) >= deadZone)
                {
                    mouseSpeedY = yAxis;
                }
                else
                {
                    mouseSpeedY = 0.0F;
                }
            }

            if(Math.abs(mouseSpeedX) > 0.05F || Math.abs(mouseSpeedY) > 0.05F)
            {
                double mouseSpeed = Controllable.getOptions().getMouseSpeed() * mc.getMainWindow().getGuiScaleFactor();
                targetMouseX += mouseSpeed * mouseSpeedX;
                targetMouseX = MathHelper.clamp(targetMouseX, 0, mc.getMainWindow().getWidth());
                targetMouseY += mouseSpeed * mouseSpeedY;
                targetMouseY = MathHelper.clamp(targetMouseY, 0, mc.getMainWindow().getHeight());
                lastUse = 100;
                moved = true;
            }

            prevXAxis = controller.getLThumbStickXValue();
            prevYAxis = controller.getLThumbStickYValue();

            this.moveMouseToClosestSlot(moving, mc.currentScreen);

            if(mc.currentScreen instanceof CreativeScreen)
            {
                this.handleCreativeScrolling((CreativeScreen) mc.currentScreen, controller);
            }

            if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse())
            {
                Screen screen = mc.currentScreen;
                if(screen != null && (targetMouseX != prevTargetMouseX || targetMouseY != prevTargetMouseY))
                {
                    if(mc.loadingGui == null)
                    {
                        double mouseX = virtualMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
                        double mouseY = virtualMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
                        Screen.wrapScreenError(() -> screen.mouseMoved(mouseX, mouseY), "mouseMoved event handler", ((IGuiEventListener) screen).getClass().getCanonicalName());
                        if(mc.mouseHelper.activeButton != -1 && mc.mouseHelper.eventTime > 0.0D)
                        {
                            double dragX = (targetMouseX - prevTargetMouseX) * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
                            double dragY = (targetMouseY - prevTargetMouseY) * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
                            Screen.wrapScreenError(() -> {
                                if(net.minecraftforge.client.ForgeHooksClient.onGuiMouseDragPre(screen, mouseX, mouseY, mc.mouseHelper.activeButton, dragX, dragY))
                                {
                                    return;
                                }
                                if(((IGuiEventListener) screen).mouseDragged(mouseX, mouseY, mc.mouseHelper.activeButton, dragX, dragY))
                                {
                                    return;
                                }
                                net.minecraftforge.client.ForgeHooksClient.onGuiMouseDragPost(screen, mouseX, mouseY, mc.mouseHelper.activeButton, dragX, dragY);
                            }, "mouseDragged event handler", ((IGuiEventListener) screen).getClass().getCanonicalName());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onScreenInit(GuiOpenEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.currentScreen == null)
        {
            nearSlot = false;
            moved = false;
            mouseSpeedX = 0.0;
            mouseSpeedY = 0.0;
            virtualMouseX = targetMouseX = prevTargetMouseX = (int) (mc.getMainWindow().getWidth() / 2F);
            virtualMouseY = targetMouseY = prevTargetMouseY = (int) (mc.getMainWindow().getHeight() / 2F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getInstance();
        if(mc.currentScreen != null && (targetMouseX != prevTargetMouseX || targetMouseY != prevTargetMouseY))
        {
            if(!(mc.currentScreen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
                double mouseX = (prevTargetMouseX + (targetMouseX - prevTargetMouseX) * partialTicks + 0.5);
                double mouseY = (prevTargetMouseY + (targetMouseY - prevTargetMouseY) * partialTicks + 0.5);
                if(Controllable.getOptions().isVirtualMouse())
                {
                    virtualMouseX = mouseX;
                    virtualMouseY = mouseY;
                }
                else
                {
                    GLFW.glfwSetCursorPos(mc.getMainWindow().getHandle(), mouseX, mouseY);
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && lastUse > 0)
        {
            RenderSystem.pushMatrix();
            {
                CursorType type = Controllable.getOptions().getCursorType();
                Minecraft minecraft = event.getGui().getMinecraft();
                if(minecraft.player == null || (minecraft.player.inventory.getItemStack().isEmpty() || type == CursorType.CONSOLE))
                {
                    double mouseX = (prevTargetMouseX + (targetMouseX - prevTargetMouseX) * Minecraft.getInstance().getRenderPartialTicks());
                    double mouseY = (prevTargetMouseY + (targetMouseY - prevTargetMouseY) * Minecraft.getInstance().getRenderPartialTicks());
                    RenderSystem.translated(mouseX / minecraft.getMainWindow().getGuiScaleFactor(), mouseY / minecraft.getMainWindow().getGuiScaleFactor(), 500);
                    RenderSystem.color3f(1.0F, 1.0F, 1.0F);
                    RenderSystem.disableLighting();
                    event.getGui().getMinecraft().getTextureManager().bindTexture(CURSOR_TEXTURE);
                    if(type == CursorType.CONSOLE)
                    {
                        RenderSystem.scaled(0.5, 0.5, 0.5);
                    }
                    Screen.blit(-8, -8, 16, 16, nearSlot ? 16 : 0, type.ordinal() * 16, 16, 16, 32, CursorType.values().length * 16);
                }
            }
            RenderSystem.popMatrix();
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if(player == null)
            return;

        if(mc.currentScreen == null && (targetYaw != 0F || targetPitch != 0F))
        {
            float elapsedTicks = Minecraft.getInstance().getTickLength();
            player.rotateTowards((targetYaw / 0.15) * elapsedTicks, (targetPitch / 0.15) * (Controllable.getOptions().isInvertLook() ? -1 : 1) * elapsedTicks);
            if(player.getRidingEntity() != null)
            {
                player.getRidingEntity().applyOrientationToEntity(player);
            }
        }
    }

    private Vec2f handleAimAssist(float targetYaw, float targetPitch)
    {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        Controller controller = Controllable.getController();

        if(player == null || controller == null || mouseMoved)
        {
            return new Vec2f(targetPitch, targetYaw);
        }

        float resultPitch = targetPitch;
        float resultYaw = targetYaw;

        boolean entityInRange = mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY; // Is true if an entity is in the crosshair range

        boolean targetInRange = false; // Is true if the target is in the crosshair raytrace

        // Change aim assist target if new one in sight
        if (entityInRange) {
            EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult) mc.objectMouseOver;



            if (entityRayTraceResult.getEntity() instanceof LivingEntity)
            {
                ControllerOptions.AimAssistMode mode = getMode(entityRayTraceResult.getEntity());
                if(mode != null && mode.aim())
                {
                    aimAssistTarget = entityRayTraceResult.getEntity();
                    targetInRange = true;
                }
            }
        }

        if (aimAssistTarget == null || // Avoid null pointers
                !aimAssistTarget.isAlive() ||
                aimAssistTarget.getDistance(player) >= 5.9 // A little higher than 5 to handle when it gets farther
        ) aimAssistTarget = null; // Remove target when it's out of range

        if(aimAssistTarget != null && aimAssistTarget.isAlive())
        {
            float aimAssistTargetDistance = aimAssistTarget.getDistance(player);

            ControllerOptions.AimAssistMode mode = getMode(aimAssistTarget); // Aim assist mode

            if(mode != null && mode != ControllerOptions.AimAssistMode.NONE && aimAssistTargetDistance <= 5.2 && player.canEntityBeSeen(aimAssistTarget)) // Avoid checking entities such as drops or tnt
            {

                // intensity as percent decimal
                float assistIntensity = Controllable.getOptions().getAimAssistIntensity() / 100.0f;

                // Lower sensitivity when in bounding box
                if(mode.sensitivity() && targetInRange && controllerInput)
                {

                    float invertedIntensity = 1.0f - assistIntensity; // 1.0 - 1.0 max intensity // 1.0 - 0 = the least intensity

                    if (invertedIntensity == 0) invertedIntensity = 0.009f;

                    double multiplier = 0.90 * (invertedIntensity);



                    resultYaw *= (float) (multiplier); // Slows the sensitivity to stop slingshotting the bounding box. It can still be slingshotted though if attempted.
                    resultPitch *= (float) (multiplier); // Slows the sensitivity to stop slingshotting the bounding box. It can still be slingshotted though if attempted.
                }




                //Check if mouse moves. This is to avoid tracking an entity with a mouse as it will be considered hacks.

                if (controllerInput && !mouseMoved) aimAssistIgnore = false;
                if (mouseMoved) aimAssistIgnore = true;

                if(mode.aim() && !aimAssistIgnore)
                {
                    float yaw = MathHelper.wrapDegrees(getTargetYaw(aimAssistTarget, player));
                    float pitch = MathHelper.wrapDegrees(getTargetPitch(aimAssistTarget, player));

                    float calcPitch = (float) (MathHelper.wrapSubtractDegrees(player.rotationPitch, pitch) * 0.38 * assistIntensity);
                    float calcYaw = (float) (MathHelper.wrapSubtractDegrees(player.rotationYaw, yaw) * 0.46 * assistIntensity);

                    float invertedIntensity = (float) (1.0 - assistIntensity); // 1.0 - 1.0 max intensity // 1.0 - 0 = the least intensity

                    float yawDistance = 7.2f;
                    yawDistance -= yawDistance * invertedIntensity; // Adjust distance accordingly to assist intensity
                    float pitchDistance = 5.8f;
                    pitchDistance -= pitchDistance * invertedIntensity;  // Adjust distance accordingly to assist intensity

                    // Only track when entity is in view
                    if (Math.abs(calcYaw) <= yawDistance && Math.abs(calcPitch) <= pitchDistance) {

                        if (Math.abs(calcYaw) <= yawDistance)
                        {
                            resultYaw += calcYaw;
                        }

                        if (Math.abs(calcPitch) <= pitchDistance)
                        {
                            resultPitch += calcPitch;
                        }
                    }


                    //                    if(MathHelper.normalizeAngle(calcPitch))
                    //                    {
                    //
                    //                    }
                }
            }
        }

        return new Vec2f(resultPitch, resultYaw);
    }

    /**
     * From {@link LookController#getTargetPitch()}
     * @param target
     * @param playerEntity
     * @return
     */
    protected float getTargetPitch(Entity target, PlayerEntity playerEntity) {
        double xDiff = target.getPosX() - playerEntity.getPosX();
        double yDiff = getEyePosition(target) - playerEntity.func_226280_cw_();
        double zDiff = target.getPosZ() - playerEntity.getPosZ();
        double distance = MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
        return (float) (-(MathHelper.atan2(yDiff, distance) * (double)(180F / (float)Math.PI)));
    }

    /**
     * From {@link LookController#getTargetYaw()}
     * @param target
     * @param playerEntity
     * @return
     */
    protected float getTargetYaw(Entity target, PlayerEntity playerEntity) {
        double diffX = target.getPosX() - playerEntity.getPosX();
        double diffZ = target.getPosZ() - playerEntity.getPosZ();
        return (float)(MathHelper.atan2(diffZ, diffX) * (double)(180F / (float)Math.PI)) - 90.0F;
    }

    /**
     * From {@link LookController#getEyePosition(Entity)}
     * @param entity
     * @return
     */
    private static double getEyePosition(Entity entity) {
        return entity instanceof LivingEntity ? entity.func_226280_cw_() : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;
    }

    private ControllerOptions.AimAssistMode getMode(Entity entity)
    {
        if(entity instanceof PlayerEntity)
        {
            return Controllable.getOptions().getPlayerAimMode();
        }

        if(entity instanceof MonsterEntity || entity instanceof SlimeEntity || entity instanceof PhantomEntity)
        {
            return Controllable.getOptions().getHostileAimMode();
        }

        if(entity instanceof AnimalEntity || entity instanceof AmbientEntity || entity instanceof WaterMobEntity)
        {
            return Controllable.getOptions().getAnimalAimMode();
        }

        return null;
    }

    @SubscribeEvent
    public void onRender(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
            return;

        targetYaw = 0F;
        targetPitch = 0F;

        Minecraft mc = Minecraft.getInstance();

        mouseMoved |= Math.abs(rawMouseX - mc.mouseHelper.getMouseX()) > 0.05 || Math.abs(rawMouseY - mc.mouseHelper.getMouseY()) > 0.05;

        rawMouseX = mc.mouseHelper.getMouseX();
        rawMouseY = mc.mouseHelper.getMouseY();

        PlayerEntity player = mc.player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        float deadZone = (float) Controllable.getOptions().getDeadZone();
        controllerInput = (Math.abs(controller.getRThumbStickXValue()) >= deadZone || Math.abs(controller.getRThumbStickYValue()) >= deadZone); // True if controller has been moved

        mouseMoved &= !controllerInput; // true if both are true
        controllerInput &= !mouseMoved; // true if both are true

        if(mc.currentScreen == null)
        {

            /* Handles rotating the yaw of player */
            if(Math.abs(controller.getRThumbStickXValue()) >= deadZone)
            {
                lastUse = 100;
                double rotationSpeed = Controllable.getOptions().getRotationSpeed();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrim = (controller.getRThumbStickXValue() > 0 ? 1 : -1) * deadZone;
                    float rotationYaw = (turnEvent.getYawSpeed() * (controller.getRThumbStickXValue() - deadZoneTrim) / (1.0F - deadZone)) * 0.33F;
                    targetYaw = rotationYaw;
                }
            }
            if(Math.abs(controller.getRThumbStickYValue()) >= deadZone)
            {
                lastUse = 100;
                double rotationSpeed = Controllable.getOptions().getRotationSpeed();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrim = (controller.getRThumbStickYValue() > 0 ? 1 : -1) * deadZone;
                    float rotationPitch = (turnEvent.getPitchSpeed() * (controller.getRThumbStickYValue() - deadZoneTrim) / (1.0F - deadZone)) * 0.33F;
                    targetPitch = rotationPitch;
                }
            }

            //            if (targetYaw != 0 || targetPitch != 0)
            //            {



            if(Controllable.getOptions().isAimAssist())
            {
                Vec2f aimAssist = handleAimAssist(targetYaw, targetPitch);


                targetPitch = aimAssist.x;
                targetYaw = aimAssist.y;
            } else {
                aimAssistTarget = null;
            }

            //            }
        }

        if(mc.currentScreen == null)
        {
            if(ButtonRegistry.ButtonActions.DROP_ITEM.getButton().isButtonDown())
            {
                lastUse = 100;
                dropCounter++;
            }
        }

        if(dropCounter > 20)
        {
            if(!mc.player.isSpectator())
            {
                mc.player.func_225609_n_(true);
            }
            dropCounter = 0;
        }
        else if(dropCounter > 0 && !ButtonRegistry.ButtonActions.DROP_ITEM.getButton().isButtonDown())
        {
            if(!mc.player.isSpectator())
            {
                mc.player.func_225609_n_(false);
            }
            dropCounter = 0;
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();

        if(keyboardSneaking && !mc.gameSettings.keyBindSneak.isKeyDown())
        {
            sneaking = false;
            keyboardSneaking = false;
        }

        if(mc.gameSettings.keyBindSneak.isKeyDown())
        {
            sneaking = true;
            keyboardSneaking = true;
        }

        if(mc.player.abilities.isFlying || mc.player.isPassenger())
        {
            lastUse = 100;
            sneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            sneaking |= ButtonRegistry.ButtonActions.SNEAK.getButton().isButtonDown();
            isFlying = true;
        }
        else if(isFlying)
        {
            sneaking = false;
            isFlying = false;
        }

        event.getMovementInput().field_228350_h_ = sneaking;

        if(Controllable.getOptions().isToggleSprint())
        {
            if(keyboardSprinting && !mc.gameSettings.keyBindSprint.isKeyDown())
            {
                sprinting = false;
                keyboardSprinting = false;
            }

            if(mc.gameSettings.keyBindSprint.isKeyDown())
            {
                sprinting = true;
                keyboardSprinting = true;
            }

            sprinting |= mc.gameSettings.keyBindSprint.isKeyDown();

            mc.player.setSprinting(sprinting);
        }

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller)))
            {
                float deadZone = (float) Controllable.getOptions().getDeadZone();

                if(Math.abs(controller.getLThumbStickYValue()) >= deadZone)
                {
                    lastUse = 100;
                    int dir = controller.getLThumbStickYValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickYValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

                    if(event.getMovementInput().field_228350_h_)
                    {
                        event.getMovementInput().moveForward *= 0.3D;
                    }
                }

                if(Math.abs(controller.getLThumbStickXValue()) >= deadZone)
                {
                    lastUse = 100;
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickXValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

                    if(event.getMovementInput().field_228350_h_)
                    {
                        event.getMovementInput().moveStrafe *= 0.3D;
                    }
                }
            }

            if(ButtonRegistry.ButtonActions.JUMP.getButton().isButtonDown())
            {
                event.getMovementInput().jump = true;
            }

            // Held down sprint
            if(ButtonRegistry.ButtonActions.SPRINT.getButton().isButtonDown() && !Controllable.getOptions().isToggleSprint())
            {
                player.setSprinting(true);
            }

            // Reset timer if it reaches target
            if(currentAttackTimer > Controllable.getOptions().getAttackSpeed())
                currentAttackTimer = 0;

            if(ButtonRegistry.ButtonActions.USE_ITEM.getButton().isButtonDown() && mc.rightClickDelayTimer == 0 && !mc.player.isHandActive())
            {
                mc.rightClickMouse();
            }

            else if(ButtonRegistry.ButtonActions.ATTACK.getButton().isButtonDown() && mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY && currentAttackTimer == 0)
            {
                // This is to keep attacking while the button is held and staring at a mob
                mc.clickMouse();
                currentAttackTimer = 1;
            }

            // Keep the timer going if the first attack was registered
            // This is to avoid only increasing timer while staring at a mob.
            if(ButtonRegistry.ButtonActions.ATTACK.getButton().isButtonDown() && currentAttackTimer > 0)
            {
                currentAttackTimer++;
            }

            // Reset timer when button is no longer held
            if(!ButtonRegistry.ButtonActions.ATTACK.getButton().isButtonDown())
            {
                currentAttackTimer = 0;
            }
        }

    }

    public void handleButtonInput(Controller controller, int button, boolean state)
    {
        if(Minecraft.getInstance().currentScreen instanceof ControllerLayoutScreen)
        {
            return;
        }

        lastUse = 100;

        ControllerEvent.ButtonInput eventInput = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(eventInput))
            return;

        button = eventInput.getModifiedButton();
        ButtonBinding.setButtonState(button, state);

        ControllerEvent.Button event = new ControllerEvent.Button(controller);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        Minecraft mc = Minecraft.getInstance();
        if(state)
        {
            if(mc.currentScreen == null)
            {
                if(ButtonRegistry.ButtonActions.SPRINT.getButton().isButtonPressed())
                {
                    if(Controllable.getOptions().isToggleSprint() && mc.player != null)
                    {
                        sprinting = !sprinting;
                    }
                }
                else if(ButtonRegistry.ButtonActions.INVENTORY.getButton().isButtonPressed())
                {
                    if(mc.playerController.isRidingHorse())
                    {
                        mc.player.sendHorseInventory();
                    }
                    else
                    {
                        mc.getTutorial().openInventory();
                        mc.displayGuiScreen(new InventoryScreen(mc.player));
                    }
                }
                else if(ButtonRegistry.ButtonActions.SNEAK.getButton().isButtonPressed())
                {
                    if(mc.player != null && !mc.player.abilities.isFlying && !mc.player.isPassenger())
                    {
                        sneaking = !sneaking;
                    }
                }
                else if(ButtonRegistry.ButtonActions.SCROLL_RIGHT.getButton().isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(-1);
                    }
                }
                else if(ButtonRegistry.ButtonActions.SCROLL_LEFT.getButton().isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(1);
                    }
                }
                else if(ButtonRegistry.ButtonActions.SWAP_HANDS.getButton().isButtonPressed())
                {
                    if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null)
                    {

                        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
                    }
                }
                else if(ButtonRegistry.ButtonActions.TOGGLE_PERSPECTIVE.getButton().isButtonPressed() && mc.mouseHelper.isMouseGrabbed())
                {
                    cycleThirdPersonView();
                }
                else if(ButtonRegistry.ButtonActions.PAUSE_GAME.getButton().isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.displayInGameMenu(false);
                    }
                }
                else if(mc.player != null && !mc.player.isHandActive())
                {
                    if(ButtonRegistry.ButtonActions.ATTACK.getButton().isButtonPressed())
                    {
                        mc.clickMouse();
                        currentAttackTimer = 1;
                    }
                    else if(ButtonRegistry.ButtonActions.USE_ITEM.getButton().isButtonPressed())
                    {
                        mc.rightClickMouse();
                    }
                    else if(ButtonRegistry.ButtonActions.PICK_BLOCK.getButton().isButtonPressed())
                    {
                        mc.middleClickMouse();
                    }
                }
            }
            else
            {
                if(ButtonRegistry.ButtonActions.INVENTORY.getButton().isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.closeScreen();
                    }
                }
                else if(ButtonRegistry.ButtonActions.SCROLL_RIGHT.getButton().isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, 1);
                    }
                }
                else if(ButtonRegistry.ButtonActions.SCROLL_LEFT.getButton().isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, -1);
                    }
                }
                else if(ButtonRegistry.ButtonActions.PAUSE_GAME.getButton().isButtonPressed())
                {
                    if(mc.currentScreen instanceof IngameMenuScreen)
                    {
                        mc.displayGuiScreen(null);
                    }
                }
                else if(button == Buttons.A)
                {
                    invokeMouseClick(mc.currentScreen, 0);
                }
                else if(button == Buttons.X)
                {
                    invokeMouseClick(mc.currentScreen, 1);
                }
                else if(button == Buttons.B && mc.player != null && mc.player.inventory.getItemStack().isEmpty())
                {
                    invokeMouseClick(mc.currentScreen, 0);
                }
            }
        }
        else
        {
            if(mc.currentScreen == null)
            {

            }
            else
            {
                if(button == Buttons.A)
                {
                    invokeMouseReleased(mc.currentScreen, 0);
                }
                else if(button == Buttons.X)
                {
                    invokeMouseReleased(mc.currentScreen, 1);
                }
            }
        }
    }

    /**
     * Cycles the third person view. Minecraft doesn't have this code in a convenient method.
     */
    private void cycleThirdPersonView()
    {
        Minecraft mc = Minecraft.getInstance();

        mc.gameSettings.thirdPersonView++;
        if(mc.gameSettings.thirdPersonView > 2)
        {
            mc.gameSettings.thirdPersonView = 0;
        }

        if(mc.gameSettings.thirdPersonView == 0)
        {
            mc.gameRenderer.loadEntityShader(mc.getRenderViewEntity());
        }
        else if(mc.gameSettings.thirdPersonView == 1)
        {
            mc.gameRenderer.loadEntityShader(null);
        }
    }

    private void scrollCreativeTabs(CreativeScreen creative, int dir)
    {
        lastUse = 100;

        try
        {
            Method method = ObfuscationReflectionHelper.findMethod(CreativeScreen.class, "func_147050_b", ItemGroup.class);
            method.setAccessible(true);
            if(dir > 0)
            {
                if(creative.getSelectedTabIndex() < ItemGroup.GROUPS.length - 1)
                {
                    method.invoke(creative, ItemGroup.GROUPS[creative.getSelectedTabIndex() + 1]);
                }
            }
            else if(dir < 0)
            {
                if(creative.getSelectedTabIndex() > 0)
                {
                    method.invoke(creative, ItemGroup.GROUPS[creative.getSelectedTabIndex() - 1]);
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void moveMouseToClosestSlot(boolean moving, Screen screen)
    {
        nearSlot = false;

        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof ContainerScreen)
        {
            /* Prevents cursor from moving until at least some input is detected */
            if(!moved)
                return;

            Minecraft mc = Minecraft.getInstance();
            ContainerScreen guiContainer = (ContainerScreen) screen;
            int guiLeft = (guiContainer.width - guiContainer.getXSize()) / 2;
            int guiTop = (guiContainer.height - guiContainer.getYSize()) / 2;
            int mouseX = (int) (targetMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth());
            int mouseY = (int) (targetMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight());

            //Slot closestSlot = guiContainer.getSlotUnderMouse();

            /* Finds the closest slot in the GUI within 14 pixels (inclusive) */
            Slot closestSlot = null;
            double closestDistance = -1.0;
            for(Slot slot : guiContainer.getContainer().inventorySlots)
            {
                int posX = guiLeft + slot.xPos + 8;
                int posY = guiTop + slot.yPos + 8;

                double distance = Math.sqrt(Math.pow(posX - mouseX, 2) + Math.pow(posY - mouseY, 2));
                if((closestDistance == -1.0 || distance < closestDistance) && distance <= 14.0)
                {
                    closestSlot = slot;
                    closestDistance = distance;
                }
            }

            if(closestSlot != null && (closestSlot.getHasStack() || !mc.player.inventory.getItemStack().isEmpty()))
            {
                nearSlot = true;
                int slotCenterXScaled = guiLeft + closestSlot.xPos + 8;
                int slotCenterYScaled = guiTop + closestSlot.yPos + 8;
                int slotCenterX = (int) (slotCenterXScaled / ((double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth()));
                int slotCenterY = (int) (slotCenterYScaled / ((double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight()));
                double deltaX = slotCenterX - targetMouseX;
                double deltaY = slotCenterY - targetMouseY;

                if(!moving)
                {
                    if(mouseX != slotCenterXScaled || mouseY != slotCenterYScaled)
                    {
                        targetMouseX += deltaX * 0.75;
                        targetMouseY += deltaY * 0.75;
                    }
                    else
                    {
                        mouseSpeedX = 0.0F;
                        mouseSpeedY = 0.0F;
                    }
                }

                mouseSpeedX *= 0.75F;
                mouseSpeedY *= 0.75F;
            }
            else
            {
                mouseSpeedX *= 0.1F;
                mouseSpeedY *= 0.1F;
            }
        }
        else
        {
            mouseSpeedX = 0.0F;
            mouseSpeedY = 0.0F;
        }
    }

    private void handleCreativeScrolling(CreativeScreen creative, Controller controller)
    {
        try
        {
            int i = (creative.getContainer().itemList.size() + 9 - 1) / 9 - 5;
            int dir = 0;

            if(controller.getSDL2Controller().getButton(SDL_CONTROLLER_BUTTON_DPAD_UP) || controller.getRThumbStickYValue() <= -0.8F)
            {
                dir = 1;
            }
            else if(controller.getSDL2Controller().getButton(SDL_CONTROLLER_BUTTON_DPAD_DOWN) || controller.getRThumbStickYValue() >= 0.8F)
            {
                dir = -1;
            }

            Field field = ObfuscationReflectionHelper.findField(CreativeScreen.class, "field_147067_x");
            field.setAccessible(true);

            float currentScroll = field.getFloat(creative);
            currentScroll = (float) ((double) currentScroll - (double) dir / (double) i);
            currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
            field.setFloat(creative, currentScroll);
            creative.getContainer().scrollTo(currentScroll);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Invokes a mouse click in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseClick(Screen screen, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            double mouseX = mc.mouseHelper.getMouseX();
            double mouseY = mc.mouseHelper.getMouseY();
            if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && lastUse > 0)
            {
                mouseX = virtualMouseX;
                mouseY = virtualMouseY;
            }
            mouseX = mouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
            mouseY = mouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();

            mc.mouseHelper.activeButton = button;
            mc.mouseHelper.eventTime = NativeUtil.getTime();

            double finalMouseX = mouseX;
            double finalMouseY = mouseY;
            Screen.wrapScreenError(() -> {
                boolean cancelled = ForgeHooksClient.onGuiMouseClickedPre(screen, finalMouseX, finalMouseY, button);
                if(!cancelled)
                {
                    cancelled = screen.mouseClicked(finalMouseX, finalMouseY, button);
                }
                if(!cancelled)
                {
                    ForgeHooksClient.onGuiMouseClickedPost(screen, finalMouseX, finalMouseY, button);
                }
            }, "mouseClicked event handler", screen.getClass().getCanonicalName());
        }
    }

    /**
     * Invokes a mouse released in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseReleased(Screen screen, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            double mouseX = mc.mouseHelper.getMouseX();
            double mouseY = mc.mouseHelper.getMouseY();
            if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && lastUse > 0)
            {
                mouseX = virtualMouseX;
                mouseY = virtualMouseY;
            }
            mouseX = mouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
            mouseY = mouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();

            mc.mouseHelper.activeButton = -1;

            double finalMouseX = mouseX;
            double finalMouseY = mouseY;
            Screen.wrapScreenError(() -> {
                boolean cancelled = ForgeHooksClient.onGuiMouseReleasedPre(screen, finalMouseX, finalMouseY, button);
                if(!cancelled)
                {
                    cancelled = screen.mouseReleased(finalMouseX, finalMouseY, button);
                }
                if(!cancelled)
                {
                    ForgeHooksClient.onGuiMouseReleasedPost(screen, finalMouseX, finalMouseY, button);
                }
            }, "mouseReleased event handler", screen.getClass().getCanonicalName());
        }
    }

    public boolean isSneaking()
    {
        return sneaking;
    }

    public boolean isSprinting()
    {
        return sprinting;
    }
}
