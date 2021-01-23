package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.event.ControllerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
    private boolean sneaking = false;
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

    private int dropCounter = -1;

    public double getVirtualMouseX()
    {
        return this.virtualMouseX;
    }

    public double getVirtualMouseY()
    {
        return this.virtualMouseY;
    }

    public int getLastUse()
    {
        return this.lastUse;
    }

    public void resetLastUse()
    {
        this.lastUse = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            this.prevTargetMouseX = this.targetMouseX;
            this.prevTargetMouseY = this.targetMouseY;

            if(this.lastUse > 0)
            {
                this.lastUse--;
            }

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            if(Math.abs(controller.getLTriggerValue()) >= 0.1F || Math.abs(controller.getRTriggerValue()) >= 0.1F)
            {
                this.lastUse = 100;
            }

            Minecraft mc = Minecraft.getInstance();
            if(mc.mouseHelper.isMouseGrabbed())
                return;

            if(mc.currentScreen == null || mc.currentScreen instanceof ControllerLayoutScreen)
                return;

            float deadZone = (float) Math.min(1.0F, Config.CLIENT.options.deadZone.get() + 0.25F);

            /* Only need to run code if left thumb stick has input */
            boolean moving = Math.abs(controller.getLThumbStickXValue()) >= deadZone || Math.abs(controller.getLThumbStickYValue()) >= deadZone;
            if(moving)
            {
                /* Updates the target mouse position when the initial thumb stick movement is
                 * detected. This fixes an issue when the user moves the cursor with the mouse then
                 * switching back to controller, the cursor would jump to old target mouse position. */
                if(Math.abs(this.prevXAxis) < deadZone && Math.abs(this.prevYAxis) < deadZone)
                {
                    double mouseX = mc.mouseHelper.getMouseX();
                    double mouseY = mc.mouseHelper.getMouseY();
                    if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get())
                    {
                        mouseX = this.virtualMouseX;
                        mouseY = this.virtualMouseY;
                    }
                    this.prevTargetMouseX = this.targetMouseX = (int) mouseX;
                    this.prevTargetMouseY = this.targetMouseY = (int) mouseY;
                }

                float xAxis = (controller.getLThumbStickXValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickXValue());
                if(Math.abs(xAxis) >= deadZone)
                {
                    this.mouseSpeedX = xAxis;
                }
                else
                {
                    this.mouseSpeedX = 0.0F;
                }

                float yAxis = (controller.getLThumbStickYValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickYValue());
                if(Math.abs(yAxis) >= deadZone)
                {
                    this.mouseSpeedY = yAxis;
                }
                else
                {
                    this.mouseSpeedY = 0.0F;
                }
            }

            if(Math.abs(this.mouseSpeedX) > 0F || Math.abs(this.mouseSpeedY) > 0F)
            {
                double mouseSpeed = Config.CLIENT.options.mouseSpeed.get() * mc.getMainWindow().getGuiScaleFactor();

                // When hovering over slots, slows down the mouse speed to make it easier
                if(mc.currentScreen instanceof ContainerScreen)
                {
                    ContainerScreen screen = (ContainerScreen) mc.currentScreen;
                    if(screen.getSlotUnderMouse() != null)
                    {
                        mouseSpeed *= 0.5;
                    }
                }

                double mouseX = this.virtualMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
                double mouseY = this.virtualMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
                IGuiEventListener hoveredListener = mc.currentScreen.children().stream().filter(o -> o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
                if(hoveredListener != null && !(hoveredListener instanceof AbstractList))
                {
                    mouseSpeed *= 0.4;
                }

                this.targetMouseX += mouseSpeed * this.mouseSpeedX;
                this.targetMouseX = MathHelper.clamp(this.targetMouseX, 0, mc.getMainWindow().getWidth());
                this.targetMouseY += mouseSpeed * this.mouseSpeedY;
                this.targetMouseY = MathHelper.clamp(this.targetMouseY, 0, mc.getMainWindow().getHeight());
                this.lastUse = 100;
                this.moved = true;
            }

            this.prevXAxis = controller.getLThumbStickXValue();
            this.prevYAxis = controller.getLThumbStickYValue();

            this.moveMouseToClosestSlot(moving, mc.currentScreen);

            if(mc.currentScreen instanceof CreativeScreen)
            {
                this.handleCreativeScrolling((CreativeScreen) mc.currentScreen, controller);
            }

            if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get())
            {
                Screen screen = mc.currentScreen;
                if(screen != null && (this.targetMouseX != this.prevTargetMouseX || this.targetMouseY != this.prevTargetMouseY))
                {
                    if(mc.loadingGui == null)
                    {
                        double mouseX = this.virtualMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
                        double mouseY = this.virtualMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
                        Screen.wrapScreenError(() -> screen.mouseMoved(mouseX, mouseY), "mouseMoved event handler", ((IGuiEventListener) screen).getClass().getCanonicalName());
                        if(mc.mouseHelper.activeButton != -1 && mc.mouseHelper.eventTime > 0.0D)
                        {
                            double dragX = (this.targetMouseX - this.prevTargetMouseX) * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
                            double dragY = (this.targetMouseY - this.prevTargetMouseY) * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
                            Screen.wrapScreenError(() ->
                            {
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
            this.nearSlot = false;
            this.moved = false;
            this.mouseSpeedX = 0.0;
            this.mouseSpeedY = 0.0;
            this.virtualMouseX = this.targetMouseX = this.prevTargetMouseX = (int) (mc.getMainWindow().getWidth() / 2F);
            this.virtualMouseY = this.targetMouseY = this.prevTargetMouseY = (int) (mc.getMainWindow().getHeight() / 2F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getInstance();
        if(mc.currentScreen != null && (this.targetMouseX != this.prevTargetMouseX || this.targetMouseY != this.prevTargetMouseY))
        {
            if(!(mc.currentScreen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
                double mouseX = (this.prevTargetMouseX + (this.targetMouseX - this.prevTargetMouseX) * partialTicks + 0.5);
                double mouseY = (this.prevTargetMouseY + (this.targetMouseY - this.prevTargetMouseY) * partialTicks + 0.5);
                if(Config.CLIENT.options.virtualMouse.get())
                {
                    this.virtualMouseX = mouseX;
                    this.virtualMouseY = mouseY;
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
        if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && lastUse > 0)
        {
            RenderSystem.pushMatrix();
            {
                CursorType type = Config.CLIENT.options.cursorType.get();
                Minecraft minecraft = event.getGui().getMinecraft();
                if(minecraft.player == null || (minecraft.player.inventory.getItemStack().isEmpty() || type == CursorType.CONSOLE))
                {
                    double mouseX = (this.prevTargetMouseX + (this.targetMouseX - this.prevTargetMouseX) * Minecraft.getInstance().getRenderPartialTicks());
                    double mouseY = (this.prevTargetMouseY + (this.targetMouseY - this.prevTargetMouseY) * Minecraft.getInstance().getRenderPartialTicks());
                    RenderSystem.translated(mouseX / minecraft.getMainWindow().getGuiScaleFactor(), mouseY / minecraft.getMainWindow().getGuiScaleFactor(), 500);
                    RenderSystem.color3f(1.0F, 1.0F, 1.0F);
                    RenderSystem.disableLighting();
                    event.getGui().getMinecraft().getTextureManager().bindTexture(CURSOR_TEXTURE);
                    if(type == CursorType.CONSOLE)
                    {
                        RenderSystem.scaled(0.5, 0.5, 0.5);
                    }
                    Screen.blit(event.getMatrixStack(), -8, -8, 16, 16, this.nearSlot ? 16 : 0, type.ordinal() * 16, 16, 16, 32, CursorType.values().length * 16);
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
        double mouseX = this.virtualMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
        double mouseY = this.virtualMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();
        if(mc.currentScreen != null)
        {
            IGuiEventListener hoveredListener = mc.currentScreen.children().stream().filter(o -> o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
            if(hoveredListener instanceof AbstractList)
            {
                this.handleListScrolling((AbstractList) hoveredListener, controller);
            }
        }

        PlayerEntity player = mc.player;
        if(player == null)
            return;

        if(mc.currentScreen == null && (this.targetYaw != 0F || this.targetPitch != 0F))
        {
            float elapsedTicks = Minecraft.getInstance().getTickLength();
            player.rotateTowards((this.targetYaw / 0.15) * elapsedTicks, (this.targetPitch / 0.15) * (Config.CLIENT.options.invertLook.get() ? -1 : 1) * elapsedTicks);
            if(player.getRidingEntity() != null)
            {
                player.getRidingEntity().applyOrientationToEntity(player);
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
            return;

        this.targetYaw = 0F;
        this.targetPitch = 0F;

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(mc.currentScreen == null)
        {
            float deadZone = Config.CLIENT.options.deadZone.get().floatValue();

            /* Handles rotating the yaw of player */
            if(Math.abs(controller.getRThumbStickXValue()) >= deadZone)
            {
                this.lastUse = 100;
                double rotationSpeed = Config.CLIENT.options.rotationSpeed.get();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrimX = (controller.getRThumbStickXValue() > 0 ? 1 : -1) * deadZone;
                    this.targetYaw = (turnEvent.getYawSpeed() * (controller.getRThumbStickXValue() - deadZoneTrimX) / (1.0F - deadZone)) * 0.33F;
                }
            }

            if(Math.abs(controller.getRThumbStickYValue()) >= deadZone)
            {
                this.lastUse = 100;
                double rotationSpeed = Config.CLIENT.options.rotationSpeed.get();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrimY = (controller.getRThumbStickYValue() > 0 ? 1 : -1) * deadZone;
                    this.targetPitch = (turnEvent.getPitchSpeed() * (controller.getRThumbStickYValue() - deadZoneTrimY) / (1.0F - deadZone)) * 0.33F;
                }
            }
        }

        if(mc.currentScreen == null)
        {
            if(ButtonBindings.DROP_ITEM.isButtonDown())
            {
                this.lastUse = 100;
                this.dropCounter++;
            }
        }

        if(this.dropCounter > 20)
        {
            if (!mc.player.isSpectator())
            {
                mc.player.drop(true); //TODO test
            }
            this.dropCounter = 0;
        }
        else if(this.dropCounter > 0 && !ButtonBindings.DROP_ITEM.isButtonDown())
        {
            if (!mc.player.isSpectator())
            {
                mc.player.drop(false); //TODO test
            }
            this.dropCounter = 0;
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

        if(this.keyboardSneaking && !mc.gameSettings.keyBindSneak.isKeyDown())
        {
            this.sneaking = false;
            this.keyboardSneaking = false;
        }

        if(mc.gameSettings.keyBindSneak.isKeyDown())
        {
            this.sneaking = true;
            this.keyboardSneaking = true;
        }

        if(mc.player.abilities.isFlying || mc.player.isPassenger())
        {
            this.lastUse = 100;
            this.sneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            this.sneaking |= ButtonBindings.SNEAK.isButtonDown();
            this.isFlying = true;
        }
        else if(this.isFlying)
        {
            this.sneaking = false;
            this.isFlying = false;
        }

        event.getMovementInput().sneaking = this.sneaking;

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller)))
            {
                float deadZone = Config.CLIENT.options.deadZone.get().floatValue();

                if(Math.abs(controller.getLThumbStickYValue()) >= deadZone)
                {
                    this.lastUse = 100;
                    int dir = controller.getLThumbStickYValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickYValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

                    if(event.getMovementInput().sneaking)
                    {
                        event.getMovementInput().moveForward *= 0.3D;
                    }
                }

                if(Math.abs(controller.getLThumbStickXValue()) >= deadZone)
                {
                    this.lastUse = 100;
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickXValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

                    if(event.getMovementInput().sneaking)
                    {
                        event.getMovementInput().moveStrafe *= 0.3D;
                    }
                }
            }

            if(ButtonBindings.JUMP.isButtonDown())
            {
                event.getMovementInput().jump = true;
            }
        }

        if(ButtonBindings.USE_ITEM.isButtonDown() && mc.rightClickDelayTimer == 0 && !mc.player.isHandActive())
        {
            mc.rightClickMouse();
        }
    }

    public void handleButtonInput(Controller controller, int button, boolean state)
    {
        if(Minecraft.getInstance().currentScreen instanceof ControllerLayoutScreen)
        {
            return;
        }

        this.lastUse = 100;

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
                if(ButtonBindings.INVENTORY.isButtonPressed())
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
                else if(ButtonBindings.SPRINT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.setSprinting(true);
                    }
                }
                else if(ButtonBindings.SNEAK.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.abilities.isFlying && !mc.player.isPassenger())
                    {
                        this.sneaking = !this.sneaking;
                    }
                }
                else if(ButtonBindings.SCROLL_RIGHT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(-1);
                    }
                }
                else if(ButtonBindings.SCROLL_LEFT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(1);
                    }
                }
                else if(ButtonBindings.SWAP_HANDS.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null)
                    {
                        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                    }
                }
                else if(ButtonBindings.TOGGLE_PERSPECTIVE.isButtonPressed() && mc.mouseHelper.isMouseGrabbed())
                {
                    cycleThirdPersonView();
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.displayInGameMenu(false);
                    }
                }
                else if(mc.player != null && !mc.player.isHandActive())
                {
                    if(ButtonBindings.ATTACK.isButtonPressed())
                    {
                        mc.clickMouse();
                    }
                    else if(ButtonBindings.USE_ITEM.isButtonPressed())
                    {
                        mc.rightClickMouse();
                    }
                    else if(ButtonBindings.PICK_BLOCK.isButtonPressed())
                    {
                        mc.middleClickMouse();
                    }
                }
            }
            else
            {
                if(ButtonBindings.INVENTORY.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.closeScreen();
                    }
                }
                else if(ButtonBindings.SCROLL_RIGHT.isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, 1);
                    }
                }
                else if(ButtonBindings.SCROLL_LEFT.isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, -1);
                    }
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
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
                else if(button == Buttons.B && mc.player != null)
                {
                    if(mc.player.inventory.getItemStack().isEmpty())
                    {
                        invokeMouseClick(mc.currentScreen, 0);
                    }
                    else
                    {
                        invokeMouseReleased(mc.currentScreen, 1);
                    }
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
        PointOfView pointOfView = mc.gameSettings.func_243230_g();
        mc.gameSettings.func_243229_a(pointOfView.func_243194_c());
        if(pointOfView.func_243192_a() != mc.gameSettings.func_243230_g().func_243192_a())
        {
            mc.gameRenderer.loadEntityShader(mc.gameSettings.func_243230_g().func_243192_a() ? mc.getRenderViewEntity() : null);
        }
    }

    private void scrollCreativeTabs(CreativeScreen creative, int dir)
    {
        this.lastUse = 100;

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
        this.nearSlot = false;

        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof ContainerScreen)
        {
            /* Prevents cursor from moving until at least some input is detected */
            if(!this.moved) return;

            Minecraft mc = Minecraft.getInstance();
            ContainerScreen guiContainer = (ContainerScreen) screen;
            int guiLeft = (guiContainer.width - guiContainer.getXSize()) / 2;
            int guiTop = (guiContainer.height - guiContainer.getYSize()) / 2;
            int mouseX = (int) (this.targetMouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth());
            int mouseY = (int) (this.targetMouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight());

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
                this.nearSlot = true;
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
                        this.targetMouseX += deltaX * 0.75;
                        this.targetMouseY += deltaY * 0.75;
                    }
                    else
                    {
                        this.mouseSpeedX = 0.0F;
                        this.mouseSpeedY = 0.0F;
                    }
                }

                this.mouseSpeedX *= 0.75F;
                this.mouseSpeedY *= 0.75F;
            }
            else
            {
                this.mouseSpeedX *= 0.1F;
                this.mouseSpeedY *= 0.1F;
            }
        }
        else
        {
            this.mouseSpeedX = 0.0F;
            this.mouseSpeedY = 0.0F;
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

    private void handleListScrolling(AbstractList list, Controller controller)
    {
        double dir = 0;
        if(Math.abs(controller.getRThumbStickYValue()) >= 0.2F)
        {
            this.lastUse = 100;
            dir = controller.getRThumbStickYValue();
        }
        if(controller.getSDL2Controller().getButton(SDL_CONTROLLER_BUTTON_DPAD_UP))
        {
            this.lastUse = 100;
            dir = -1.0;
        }
        else if(controller.getSDL2Controller().getButton(SDL_CONTROLLER_BUTTON_DPAD_DOWN))
        {
            this.lastUse = 100;
            dir = 1.0;
        }
        dir *= Minecraft.getInstance().getTickLength();
        list.setScrollAmount(list.getScrollAmount() + dir * 10);
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
            if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && this.lastUse > 0)
            {
                mouseX = this.virtualMouseX;
                mouseY = this.virtualMouseY;
            }
            mouseX = mouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
            mouseY = mouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();

            mc.mouseHelper.activeButton = button;
            mc.mouseHelper.eventTime = NativeUtil.getTime();

            double finalMouseX = mouseX;
            double finalMouseY = mouseY;
            Screen.wrapScreenError(() ->
            {
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
            if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && lastUse > 0)
            {
                mouseX = this.virtualMouseX;
                mouseY = this.virtualMouseY;
            }
            mouseX = mouseX * (double) mc.getMainWindow().getScaledWidth() / (double) mc.getMainWindow().getWidth();
            mouseY = mouseY * (double) mc.getMainWindow().getScaledHeight() / (double) mc.getMainWindow().getHeight();

            mc.mouseHelper.activeButton = -1;

            double finalMouseX = mouseX;
            double finalMouseY = mouseY;
            Screen.wrapScreenError(() ->
            {
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
}
