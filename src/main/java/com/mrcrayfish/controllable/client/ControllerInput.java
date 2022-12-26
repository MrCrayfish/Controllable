package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.navigation.*;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.GatherNavigationPointsEvent;
import com.mrcrayfish.controllable.integration.JEIControllablePlugin;
import com.mrcrayfish.controllable.mixin.client.CreativeModeInventoryScreenMixin;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentMixin;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ControllerInput
{
    private static final ResourceLocation CURSOR_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/cursor.png");

    private int lastUse = 0;
    private boolean keyboardSneaking = false;
    private boolean sneaking = false;
    private boolean isFlying = false;
    private boolean nearSlot = false;
    private boolean moving = false;
    private boolean preventReset;
    private boolean ignoreInput;
    private double virtualMouseX;
    private double virtualMouseY;
    private int prevMouseX;
    private int prevMouseY;
    private int mouseX;
    private int mouseY;
    private double mouseSpeedX;
    private double mouseSpeedY;
    private boolean moved;
    private float targetPitch;
    private float targetYaw;
    private long lastMerchantScroll;

    private int dropCounter = -1;

    public double getVirtualMouseX()
    {
        return this.virtualMouseX;
    }

    public double getVirtualMouseY()
    {
        return this.virtualMouseY;
    }

    private void setControllerInUse()
    {
        this.lastUse = 100;
    }

    public boolean isControllerInUse()
    {
        return this.lastUse > 0;
    }

    public int getLastUse()
    {
        return this.lastUse;
    }

    public void resetLastUse()
    {
        if(!this.preventReset)
        {
            this.lastUse = 0;
        }
        this.preventReset = false;
    }

    public boolean isMovingCursor()
    {
        return this.moving;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            this.prevMouseX = this.mouseX;
            this.prevMouseY = this.mouseY;

            if(this.lastUse > 0)
            {
                this.lastUse--;
            }

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            if((Math.abs(controller.getLTriggerValue()) >= 0.2F || Math.abs(controller.getRTriggerValue()) >= 0.2F) && !(Minecraft.getInstance().screen instanceof ControllerLayoutScreen))
            {
                this.setControllerInUse();
            }

            Minecraft mc = Minecraft.getInstance();
            if(mc.mouseHandler.isMouseGrabbed())
                return;

            if(mc.screen == null || mc.screen instanceof ControllerLayoutScreen)
                return;

            float deadZone = (float) Math.min(1.0F, Config.CLIENT.options.deadZone.get() + 0.25F);

            /* Only need to run code if left thumb stick has input */
            boolean lastMoving = this.moving;
            float xAxis = Config.CLIENT.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickXValue() : controller.getRThumbStickXValue();
            float yAxis = Config.CLIENT.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickYValue() : controller.getRThumbStickYValue();
            this.moving = Math.abs(xAxis) >= deadZone || Math.abs(yAxis) >= deadZone;
            if(this.moving)
            {
                /* Updates the target mouse position when the initial thumb stick movement is
                 * detected. This fixes an issue when the user moves the cursor with the mouse then
                 * switching back to controller, the cursor would jump to old target mouse position. */
                if(!lastMoving)
                {
                    double mouseX = mc.mouseHandler.xpos();
                    double mouseY = mc.mouseHandler.ypos();
                    if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get())
                    {
                        mouseX = this.virtualMouseX;
                        mouseY = this.virtualMouseY;
                    }
                    this.prevMouseX = this.mouseX = (int) mouseX;
                    this.prevMouseY = this.mouseY = (int) mouseY;
                }

                this.mouseSpeedX = Math.abs(xAxis) >= deadZone ? Math.signum(xAxis) * (Math.abs(xAxis) - deadZone) / (1.0F - deadZone) : 0.0F;
                this.mouseSpeedY = Math.abs(yAxis) >= deadZone ? Math.signum(yAxis) * (Math.abs(yAxis) - deadZone) / (1.0F - deadZone) : 0.0F;
                this.setControllerInUse();
            }

            if(this.lastUse <= 0)
            {
                this.mouseSpeedX = 0F;
                this.mouseSpeedY = 0F;
                return;
            }

            if(Math.abs(this.mouseSpeedX) > 0F || Math.abs(this.mouseSpeedY) > 0F)
            {
                double mouseSpeed = Config.CLIENT.options.mouseSpeed.get() * mc.getWindow().getGuiScale();

                // When hovering over slots, slows down the mouse speed to make it easier
                if(mc.screen instanceof AbstractContainerScreen<?> screen)
                {
                    if(screen.getSlotUnderMouse() != null)
                    {
                        mouseSpeed *= Config.CLIENT.options.hoverModifier.get();
                    }
                }

                double mouseX = this.virtualMouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
                double mouseY = this.virtualMouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
                List<GuiEventListener> eventListeners = new ArrayList<>(mc.screen.children());
                if(mc.screen instanceof RecipeUpdateListener)
                {
                    RecipeBookComponent recipeBook = ((RecipeUpdateListener) mc.screen).getRecipeBookComponent();
                    if(recipeBook.isVisible())
                    {
                        eventListeners.add(((RecipeBookComponentMixin) recipeBook).getFilterButton());
                        eventListeners.addAll(((RecipeBookComponentMixin) recipeBook).getRecipeTabs());
                        RecipeBookPage recipeBookPage = ((RecipeBookComponentMixin) recipeBook).getRecipeBookPage();
                        eventListeners.addAll(((RecipeBookPageAccessor) recipeBookPage).getButtons());
                        eventListeners.add(((RecipeBookPageAccessor) recipeBookPage).getForwardButton());
                        eventListeners.add(((RecipeBookPageAccessor) recipeBookPage).getBackButton());
                    }
                }

                GuiEventListener hoveredListener = eventListeners.stream().filter(o -> o != null && o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
                if(hoveredListener instanceof AbstractSelectionList<?> list)
                {
                    hoveredListener = null;
                    int count = list.children().size();
                    for(int i = 0; i < count; i++)
                    {
                        int rowTop = ReflectUtil.getAbstractListRowTop(list, i);
                        int rowBottom = ReflectUtil.getAbstractListRowBottom(list, i);
                        if(rowTop < list.getTop() && rowBottom > list.getBottom()) // Is visible
                            continue;

                        AbstractSelectionList.Entry<?> entry = list.children().get(i);
                        if(!(entry instanceof ContainerEventHandler handler))
                            continue;

                        GuiEventListener hovered = handler.children().stream().filter(o -> o != null && o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
                        if(hovered == null)
                            continue;

                        hoveredListener = hovered;
                        break;
                    }
                }
                if(hoveredListener != null)
                {
                    mouseSpeed *= Config.CLIENT.options.hoverModifier.get();
                }

                this.mouseX += mouseSpeed * this.mouseSpeedX;
                this.mouseX = Mth.clamp(this.mouseX, 0, mc.getWindow().getWidth());
                this.mouseY += mouseSpeed * this.mouseSpeedY;
                this.mouseY = Mth.clamp(this.mouseY, 0, mc.getWindow().getHeight());
                this.setControllerInUse();
                this.moved = true;
            }

            this.moveMouseToClosestSlot(this.moving, mc.screen);

            if(mc.screen instanceof CreativeModeInventoryScreen)
            {
                this.handleCreativeScrolling((CreativeModeInventoryScreen) mc.screen, controller);
            }

            if(Config.CLIENT.options.virtualMouse.get() && (this.mouseX != this.prevMouseX || this.mouseY != this.prevMouseY))
            {
                this.performMouseDrag(this.virtualMouseX, this.virtualMouseY, this.mouseX - this.prevMouseX, this.mouseY - this.prevMouseY);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onScreenInit(ScreenEvent.Opening event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null)
        {
            this.nearSlot = false;
            this.moved = false;
            this.mouseSpeedX = 0.0;
            this.mouseSpeedY = 0.0;
            this.virtualMouseX = this.mouseX = this.prevMouseX = (int) (mc.getWindow().getWidth() / 2F);
            this.virtualMouseY = this.mouseY = this.prevMouseY = (int) (mc.getWindow().getHeight() / 2F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(ScreenEvent.Render.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null && (this.mouseX != this.prevMouseX || this.mouseY != this.prevMouseY))
        {
            if(!(mc.screen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getInstance().getFrameTime();
                double mouseX = (this.prevMouseX + (this.mouseX - this.prevMouseX) * partialTicks + 0.5);
                double mouseY = (this.prevMouseY + (this.mouseY - this.prevMouseY) * partialTicks + 0.5);
                this.setMousePosition(mouseX, mouseY);
            }
        }
    }

    private void performMouseDrag(double mouseX, double mouseY, double dragX, double dragY)
    {
        if(Controllable.getController() != null)
        {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            if(screen != null)
            {
                if(mc.getOverlay() == null)
                {
                    double finalMouseX = mouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
                    double finalMouseY = mouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
                    Screen.wrapScreenError(() -> screen.mouseMoved(finalMouseX, finalMouseY), "mouseMoved event handler", ((GuiEventListener) screen).getClass().getCanonicalName());
                    if(mc.mouseHandler.activeButton != -1 && mc.mouseHandler.lastMouseEventTime > 0.0D)
                    {
                        Screen.wrapScreenError(() ->
                        {
                            double finalDragX = dragX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
                            double finalDragY = dragY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
                            if(net.minecraftforge.client.ForgeHooksClient.onScreenMouseDragPre(screen, finalMouseX, finalMouseY, mc.mouseHandler.activeButton, finalDragX, finalDragY))
                            {
                                return;
                            }
                            if(((GuiEventListener) screen).mouseDragged(finalMouseX, finalMouseY, mc.mouseHandler.activeButton, finalDragX, finalDragY))
                            {
                                return;
                            }
                            net.minecraftforge.client.ForgeHooksClient.onScreenMouseDragPost(screen, finalMouseX, finalMouseY, mc.mouseHandler.activeButton, finalDragX, finalDragY);
                        }, "mouseDragged event handler", ((GuiEventListener) screen).getClass().getCanonicalName());
                    }
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(ScreenEvent.Render.Post event)
    {
        if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && lastUse > 0)
        {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            CursorType type = Config.CLIENT.options.cursorType.get();
            Minecraft minecraft = Minecraft.getInstance();
            if(minecraft.player == null || (minecraft.player.inventoryMenu.getCarried().isEmpty() || type == CursorType.CONSOLE))
            {
                double mouseX = (this.prevMouseX + (this.mouseX - this.prevMouseX) * Minecraft.getInstance().getFrameTime());
                double mouseY = (this.prevMouseY + (this.mouseY - this.prevMouseY) * Minecraft.getInstance().getFrameTime());
                poseStack.translate(mouseX / minecraft.getWindow().getGuiScale(), mouseY / minecraft.getWindow().getGuiScale(), 500);
                RenderSystem.setShaderTexture(0, CURSOR_TEXTURE);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                if(type == CursorType.CONSOLE)
                {
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                }
                Screen.blit(poseStack, -8, -8, 16, 16, this.nearSlot ? 16 : 0, type.ordinal() * 16, 16, 16, 32, CursorType.values().length * 16);
            }
            poseStack.popPose();
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
        double mouseX = this.virtualMouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double mouseY = this.virtualMouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        if(mc.screen != null && this.lastUse > 0)
        {
            if(mc.screen instanceof MerchantScreen screen)
            {
                this.handleMerchantScrolling(screen, controller);
                return;
            }
            GuiEventListener hoveredListener = mc.screen.children().stream().filter(o -> o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
            if(hoveredListener instanceof AbstractSelectionList<?> selectionList)
            {
                this.handleListScrolling(selectionList, controller);
            }
        }

        Player player = mc.player;
        if(player == null)
            return;

        if(mc.screen == null && (this.targetYaw != 0F || this.targetPitch != 0F))
        {
            float elapsedTicks = Minecraft.getInstance().getDeltaFrameTime();
            if(!RadialMenuHandler.instance().isVisible())
            {
                player.turn((this.targetYaw / 0.15) * elapsedTicks, (this.targetPitch / 0.15) * (Config.CLIENT.options.invertLook.get() ? -1 : 1) * elapsedTicks);
            }
            if(player.getVehicle() != null)
            {
                player.getVehicle().onPassengerTurned(player);
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
        Player player = mc.player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(mc.screen == null)
        {
            float deadZone = Config.CLIENT.options.deadZone.get().floatValue();

            /* Handles rotating the yaw of player */
            if(Math.abs(controller.getRThumbStickXValue()) >= deadZone)
            {
                this.setControllerInUse();
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
                this.setControllerInUse();
                double rotationSpeed = Config.CLIENT.options.rotationSpeed.get();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrimY = (controller.getRThumbStickYValue() > 0 ? 1 : -1) * deadZone;
                    this.targetPitch = (turnEvent.getPitchSpeed() * (controller.getRThumbStickYValue() - deadZoneTrimY) / (1.0F - deadZone)) * 0.33F;
                }
            }
        }

        if(mc.screen == null)
        {
            if(ButtonBindings.DROP_ITEM.isButtonDown())
            {
                this.setControllerInUse();
                this.dropCounter++;
            }
        }

        if(this.dropCounter > 20)
        {
            if (!mc.player.isSpectator())
            {
                mc.player.drop(true);
            }
            this.dropCounter = 0;
        }
        else if(this.dropCounter > 0 && !ButtonBindings.DROP_ITEM.isButtonDown())
        {
            if (!mc.player.isSpectator())
            {
                mc.player.drop(false);
            }
            this.dropCounter = 0;
        }
    }

    @SubscribeEvent
    public void onOpenScreen(ScreenEvent.Opening event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && Config.SERVER.restrictToController.get() && !this.isControllerInUse())
        {
            if(event.getScreen() instanceof ContainerScreen)
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMouseClicked(InputEvent.MouseButton.Pre event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && (mc.screen == null || mc.screen instanceof ContainerScreen))
        {
            if(Config.SERVER.restrictToController.get())
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onInputUpdate(MovementInputUpdateEvent event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)
            return;

        if(Config.SERVER.restrictToController.get())
        {
            Input input = event.getInput();
            input.leftImpulse = 0F;
            input.forwardImpulse = 0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }

        Controller controller = Controllable.getController();
        if(controller == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if(this.keyboardSneaking && !mc.options.keyShift.isDown())
        {
            this.sneaking = false;
            this.keyboardSneaking = false;
        }

        if(Config.CLIENT.options.sneakMode.get() == SneakMode.HOLD)
        {
            this.sneaking = ButtonBindings.SNEAK.isButtonDown();
        }

        if(mc.options.keyShift.isDown())
        {
            this.sneaking = true;
            this.keyboardSneaking = true;
        }

        if(mc.player.getAbilities().flying || mc.player.isPassenger())
        {
            this.sneaking = mc.options.keyShift.isDown();
            this.sneaking |= ButtonBindings.SNEAK.isButtonDown();
            if(ButtonBindings.SNEAK.isButtonDown())
            {
                this.setControllerInUse();
            }
            this.isFlying = true;
        }
        else if(this.isFlying)
        {
            this.isFlying = false;
        }

        event.getInput().shiftKeyDown = this.sneaking;

        if(mc.screen == null)
        {
            if((!RadialMenuHandler.instance().isVisible() || Config.CLIENT.options.radialThumbstick.get() != Thumbstick.LEFT) && !MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller)))
            {
                float deadZone = Config.CLIENT.options.deadZone.get().floatValue();

                if(Math.abs(controller.getLThumbStickYValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickYValue() > 0.0F ? -1 : 1;
                    event.getInput().up = dir > 0;
                    event.getInput().down = dir < 0;
                    event.getInput().forwardImpulse = dir * Mth.clamp((Math.abs(controller.getLThumbStickYValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);
                }

                if(player.getVehicle() instanceof Boat)
                {
                    deadZone = 0.5F;
                }

                if(Math.abs(controller.getLThumbStickXValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    event.getInput().right = dir < 0;
                    event.getInput().left = dir > 0;
                    event.getInput().leftImpulse = dir * Mth.clamp((Math.abs(controller.getLThumbStickXValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);
                }
            }

            if(this.ignoreInput && !ButtonBindings.JUMP.isButtonDown())
            {
                this.ignoreInput = false;
            }

            if(ButtonBindings.JUMP.isButtonDown() && !this.ignoreInput)
            {
                event.getInput().jumping = true;
            }
        }

        if(ButtonBindings.USE_ITEM.isButtonDown() && mc.rightClickDelay == 0 && !mc.player.isUsingItem())
        {
            mc.startUseItem();
        }

        // Applies the sneaking bonus when player has Swift Sneak enchantments on boots
        float sneakBonus = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(player), 0.0F, 1.0F);
        if(player.isMovingSlowly())
        {
            event.getInput().forwardImpulse *= sneakBonus;
            event.getInput().leftImpulse *= sneakBonus;
        }
    }

    public void handleButtonInput(Controller controller, int button, boolean state, boolean virtual)
    {
        if(controller == null)
            return;

        this.setControllerInUse();

        /* We don't send event for buttons that are not bound.
         * This can happen when using the radial menu. */
        if(button != -1)
        {
            ControllerEvent.ButtonInput eventInput = new ControllerEvent.ButtonInput(controller, button, state);
            if(MinecraftForge.EVENT_BUS.post(eventInput))
                return;

            button = eventInput.getModifiedButton();
            ButtonBinding.setButtonState(button, state);
        }

        ControllerEvent.Button event = new ControllerEvent.Button(controller);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        Minecraft mc = Minecraft.getInstance();
        if(state)
        {
            if(ButtonBindings.FULLSCREEN.isButtonPressed())
            {
                mc.getWindow().toggleFullScreen();
                mc.options.fullscreen().set(mc.getWindow().isFullscreen());
                mc.options.save();
            }
            else if(ButtonBindings.SCREENSHOT.isButtonPressed())
            {
                if(mc.level != null)
                {
                    Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), (component) -> {
                        mc.execute(() -> mc.gui.getChat().addMessage(component));
                    });
                }
            }
            else if(mc.screen == null)
            {
                if(ButtonBindings.INVENTORY.isButtonPressed() && mc.gameMode != null && mc.player != null)
                {
                    if(mc.gameMode.isServerControlledInventory())
                    {
                        mc.player.sendOpenInventory();
                    }
                    else
                    {
                        mc.getTutorial().onOpenInventory();
                        mc.setScreen(new InventoryScreen(mc.player));
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
                    if(mc.player != null && !mc.player.getAbilities().flying && !this.isFlying && !mc.player.isPassenger())
                    {
                        if(Config.CLIENT.options.sneakMode.get() == SneakMode.TOGGLE)
                        {
                            this.sneaking = !this.sneaking;
                        }
                    }
                }
                else if(ButtonBindings.SCROLL_RIGHT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.getInventory().swapPaint(-1);
                    }
                }
                else if(ButtonBindings.SCROLL_LEFT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.getInventory().swapPaint(1);
                    }
                }
                else if(ButtonBindings.SWAP_HANDS.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null)
                    {
                        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                    }
                }
                else if(ButtonBindings.TOGGLE_PERSPECTIVE.isButtonPressed())
                {
                    cycleThirdPersonView();
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.pauseGame(false);
                    }
                }
                else if(ButtonBindings.ADVANCEMENTS.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.setScreen(new AdvancementsScreen(mc.player.connection.getAdvancements()));
                    }
                }
                else if(ButtonBindings.CINEMATIC_CAMERA.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.options.smoothCamera = !mc.options.smoothCamera;
                    }
                }
                else if(ButtonBindings.DEBUG_INFO.isButtonPressed())
                {
                    mc.options.renderDebug = !mc.options.renderDebug;
                }
                else if(ButtonBindings.RADIAL_MENU.isButtonPressed() && !virtual)
                {
                    RadialMenuHandler.instance().interact();
                }
                else if(mc.player != null)
                {
                    for(int i = 0; i < 9; i++)
                    {
                        if(ButtonBindings.HOTBAR_SLOTS[i].isButtonPressed())
                        {
                            mc.player.getInventory().selected = i;
                            return;
                        }
                    }

                    if(!mc.player.isUsingItem())
                    {
                        if(ButtonBindings.ATTACK.isButtonPressed())
                        {
                            mc.startAttack();
                        }
                        else if(ButtonBindings.USE_ITEM.isButtonPressed())
                        {
                            mc.startUseItem();
                        }
                        else if(ButtonBindings.PICK_BLOCK.isButtonPressed())
                        {
                            mc.pickBlock();
                        }
                    }
                }
            }
            else
            {
                if(ButtonBindings.INVENTORY.isButtonPressed())
                {
                    if(mc.screen != null)
                    {
                        // Fake an escape press for best support
                        mc.screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_ESCAPE), 0);
                    }
                }
                else if(ButtonBindings.PREVIOUS_CREATIVE_TAB.isButtonPressed())
                {
                    if(mc.screen instanceof CreativeModeInventoryScreen)
                    {
                        this.scrollCreativeTabs((CreativeModeInventoryScreen) mc.screen, 1);
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    else if(mc.screen instanceof RecipeUpdateListener listener)
                    {
                        this.scrollRecipePage(listener.getRecipeBookComponent(), 1);
                    }
                }
                else if(ButtonBindings.NEXT_CREATIVE_TAB.isButtonPressed())
                {
                    if(mc.screen instanceof CreativeModeInventoryScreen)
                    {
                        this.scrollCreativeTabs((CreativeModeInventoryScreen) mc.screen, -1);
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    else if(mc.screen instanceof RecipeUpdateListener listener)
                    {
                        this.scrollRecipePage(listener.getRecipeBookComponent(), -1);
                    }
                }
                else if(ButtonBindings.NEXT_RECIPE_TAB.isButtonPressed())
                {
                    if(mc.screen instanceof RecipeUpdateListener listener)
                    {
                        this.scrollRecipeTab(listener.getRecipeBookComponent(), -1);
                    }
                }
                else if(ButtonBindings.PREVIOUS_RECIPE_TAB.isButtonPressed())
                {
                    if(mc.screen instanceof RecipeUpdateListener listener)
                    {
                        this.scrollRecipeTab(listener.getRecipeBookComponent(), 1);
                    }
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.screen instanceof PauseScreen)
                    {
                        mc.setScreen(null);
                    }
                }
                else if(ButtonBindings.NAVIGATE_UP.isButtonPressed())
                {
                    this.navigateMouse(mc.screen, Navigate.UP);
                }
                else if(ButtonBindings.NAVIGATE_DOWN.isButtonPressed())
                {
                    this.navigateMouse(mc.screen, Navigate.DOWN);
                }
                else if(ButtonBindings.NAVIGATE_LEFT.isButtonPressed())
                {
                    this.navigateMouse(mc.screen, Navigate.LEFT);
                }
                else if(ButtonBindings.NAVIGATE_RIGHT.isButtonPressed())
                {
                    this.navigateMouse(mc.screen, Navigate.RIGHT);
                }
                else if(button == ButtonBindings.PICKUP_ITEM.getButton())
                {
                    invokeMouseClick(mc.screen, 0);

                    if(mc.screen == null)
                    {
                        this.ignoreInput = true;
                    }

                    if(Config.CLIENT.options.quickCraft.get())
                    {
                        this.craftRecipeBookItem();
                    }
                }
                else if(button == ButtonBindings.SPLIT_STACK.getButton())
                {
                    invokeMouseClick(mc.screen, 1);
                }
                else if(button == ButtonBindings.QUICK_MOVE.getButton() && mc.player != null)
                {
                    if(mc.player.inventoryMenu.getCarried().isEmpty())
                    {
                        invokeMouseClick(mc.screen, 0);
                    }
                    else
                    {
                        invokeMouseReleased(mc.screen, 1);
                    }
                }
            }
        }
        else
        {
            if(mc.screen == null)
            {

            }
            else
            {
                if(button == ButtonBindings.PICKUP_ITEM.getButton())
                {
                    invokeMouseReleased(mc.screen, 0);
                }
                else if(button == ButtonBindings.SPLIT_STACK.getButton())
                {
                    invokeMouseReleased(mc.screen, 1);
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
        CameraType cameraType = mc.options.getCameraType();
        mc.options.setCameraType(cameraType.cycle());
        if(cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson())
        {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
    }

    private void scrollCreativeTabs(CreativeModeInventoryScreen screen, int dir)
    {
        this.setControllerInUse();

        try
        {
            Method method = ObfuscationReflectionHelper.findMethod(CreativeModeInventoryScreen.class, "m_98560_", CreativeModeTab.class);
            method.setAccessible(true);
            if(dir > 0)
            {
                if(screen.getSelectedTab() < CreativeModeTab.TABS.length - 1)
                {
                    method.invoke(screen, CreativeModeTab.TABS[screen.getSelectedTab() + 1]);
                }
            }
            else if(dir < 0)
            {
                if(screen.getSelectedTab() > 0)
                {
                    method.invoke(screen, CreativeModeTab.TABS[screen.getSelectedTab() - 1]);
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void scrollRecipeTab(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookComponentMixin recipeBookMixin = ((RecipeBookComponentMixin) recipeBook);
        RecipeBookTabButton currentTab = recipeBookMixin.getCurrentTab();
        List<RecipeBookTabButton> tabs = recipeBookMixin.getRecipeTabs();
        int nextTabIndex = tabs.indexOf(currentTab) + dir;
        if(nextTabIndex >= 0 && nextTabIndex < tabs.size())
        {
            RecipeBookTabButton newTab = tabs.get(nextTabIndex);
            currentTab.setStateTriggered(false);
            recipeBookMixin.setCurrentTab(newTab);
            newTab.setStateTriggered(true);
            recipeBookMixin.invokeUpdateCollections(true);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void scrollRecipePage(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookComponentMixin) recipeBook).getRecipeBookPage();
        if(dir > 0 && page.getForwardButton().visible || dir < 0 && page.getBackButton().visible)
        {
            int currentPage = page.getCurrentPage();
            page.setCurrentPage(currentPage + dir);
            page.invokeUpdateButtonsForPage();
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void navigateMouse(Screen screen, Navigate navigate)
    {
        Minecraft mc = Minecraft.getInstance();
        int mouseX = (int) (this.mouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth());
        int mouseY = (int) (this.mouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight());

        List<NavigationPoint> points = this.gatherNavigationPoints(screen, navigate, mouseX, mouseY);

        // Gather any extra navigation points from event
        GatherNavigationPointsEvent event = new GatherNavigationPointsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        points.addAll(event.getPoints());

        // Get only the points that are in the target direction
        points.removeIf(p -> !navigate.getPredicate().test(p, mouseX, mouseY));
        if(points.isEmpty())
            return;

        Vector3d mousePos = new Vector3d(mouseX, mouseY, 0);
        Optional<NavigationPoint> minimumPointOptional = points.stream().min(navigate.getMinComparator(mouseX, mouseY));
        double minimumDelta = navigate.getKeyExtractor().apply(minimumPointOptional.get(), mousePos) + 10;
        Optional<NavigationPoint> targetPointOptional = points.stream().filter(point -> navigate.getKeyExtractor().apply(point, mousePos) <= minimumDelta).min(Comparator.comparing(p -> p.distanceTo(mouseX, mouseY)));
        if(targetPointOptional.isPresent())
        {
            NavigationPoint targetPoint = targetPointOptional.get();
            targetPoint.onNavigate();
            mc.tell(() -> // Run next frame to allow lists to update widget positions
            {
                this.performMouseDrag(this.mouseX, this.mouseY, 0, 0);
                int screenX = (int) (targetPoint.getX() / ((double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth()));
                int screenY = (int) (targetPoint.getY() / ((double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight()));
                double lastTarxpos = this.mouseX;
                double lastTarypos = this.mouseY;
                this.mouseX = this.prevMouseX = screenX;
                this.mouseY = this.prevMouseY = screenY;
                this.setMousePosition(screenX, screenY);
                if(Config.CLIENT.options.uiSounds.get())
                {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 2.0F));
                }
                this.performMouseDrag(this.mouseX, this.mouseY, screenX - lastTarxpos, screenY - lastTarypos);
            });
        }
    }

    private List<NavigationPoint> gatherNavigationPoints(Screen screen, Navigate navigate, int mouseX, int mouseY)
    {
        List<NavigationPoint> points = new ArrayList<>();

        if(screen instanceof AbstractContainerScreen<?> containerScreen)
        {
            int guiLeft = containerScreen.getGuiLeft();
            int guiTop = containerScreen.getGuiTop();
            for(Slot slot : containerScreen.getMenu().slots)
            {
                if(containerScreen.getSlotUnderMouse() == slot)
                    continue;
                int posX = guiLeft + slot.x + 8;
                int posY = guiTop + slot.y + 8;
                points.add(new SlotNavigationPoint(posX, posY, slot));
            }
        }

        List<AbstractWidget> widgets = new ArrayList<>();
        for(GuiEventListener listener : screen.children())
        {
            if(listener instanceof AbstractWidget widget && widget.active && widget.visible)
            {
                widgets.add((AbstractWidget) listener);
            }
            else if(listener instanceof AbstractSelectionList<?> list)
            {
                int count = list.children().size();
                int itemHeight = ReflectUtil.getListItemHeight(list);
                for(int i = 0; i < count; i++)
                {
                    AbstractSelectionList.Entry<?> entry = list.children().get(i);
                    int rowTop = ReflectUtil.getAbstractListRowTop(list, i);
                    int rowBottom = ReflectUtil.getAbstractListRowBottom(list, i);
                    if(rowTop > list.getTop() - itemHeight && rowBottom < list.getBottom() + itemHeight)
                    {
                        if(navigate == Navigate.UP || navigate == Navigate.DOWN)
                        {
                            points.add(new ListEntryNavigationPoint(list, entry, i));
                        }
                        if(entry instanceof ContainerEventHandler handler)
                        {
                            for(GuiEventListener child : handler.children())
                            {
                                if(child instanceof AbstractWidget widget && widget.active && widget.visible)
                                {
                                    points.add(new ListWidgetNavigationPoint(widget, list, entry));
                                }
                            }
                        }
                    }
                    else if(list.isMouseOver(mouseX, mouseY))
                    {
                        points.add(new ListEntryNavigationPoint(list, entry, i));
                    }
                }
            }
        }

        if(screen instanceof RecipeUpdateListener)
        {
            RecipeBookComponent recipeBook = ((RecipeUpdateListener) screen).getRecipeBookComponent();
            if(recipeBook.isVisible())
            {
                widgets.add(((RecipeBookComponentMixin) recipeBook).getFilterButton());
                widgets.addAll(((RecipeBookComponentMixin) recipeBook).getRecipeTabs());
                RecipeBookPage recipeBookPage = ((RecipeBookComponentMixin) recipeBook).getRecipeBookPage();
                widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).getButtons());
                widgets.add(((RecipeBookPageAccessor) recipeBookPage).getForwardButton());
                widgets.add(((RecipeBookPageAccessor) recipeBookPage).getBackButton());
            }
        }

        for(AbstractWidget widget : widgets)
        {
            if(widget == null || widget.isHoveredOrFocused() || !widget.visible || !widget.active)
                continue;
            int posX = widget.x + widget.getWidth() / 2;
            int posY = widget.y + widget.getHeight() / 2;
            points.add(new WidgetNavigationPoint(posX, posY, widget));
        }

        if(screen instanceof CreativeModeInventoryScreen)
        {
            int tabPage = CreativeModeInventoryScreenMixin.getTabPage();
            int start = tabPage * 10;
            int end = Math.min(CreativeModeTab.TABS.length, ((tabPage + 1) * 10 + 2));
            for(int i = start; i < end; i++)
            {
                CreativeModeTab group = CreativeModeTab.TABS[i];
                if(group != null)
                {
                    points.add(this.getCreativeTabPoint((CreativeModeInventoryScreen) screen, group));
                }
            }
        }

        if(Controllable.isJeiLoaded())
        {
            points.addAll(JEIControllablePlugin.getNavigationPoints());
        }

        return points;
    }

    /**
     * Gets the navigation point of a creative tab.
     */
    private BasicNavigationPoint getCreativeTabPoint(AbstractContainerScreen<?> screen, CreativeModeTab tab)
    {
        boolean topRow = tab.isTopRow();
        int column = tab.getColumn();
        int width = 28;
        int height = 32;
        int x = screen.getGuiLeft() + width * column;
        int y = screen.getGuiTop();
        x = tab.isAlignedRight() ? screen.getGuiLeft() + screen.getXSize() - width * (6 - column) : (column > 0 ? x + column : x);
        y = topRow ? y - width : y + (screen.getYSize() - 4);
        return new BasicNavigationPoint(x + width / 2.0, y + height / 2.0);
    }

    private void craftRecipeBookItem()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!(mc.screen instanceof AbstractContainerScreen<?> screen) || !(mc.screen instanceof RecipeUpdateListener listener))
            return;

        if(!listener.getRecipeBookComponent().isVisible())
            return;

        if(!(screen.getMenu() instanceof RecipeBookMenu<?>))
            return;

        RecipeBookPage recipeBookPage = ((RecipeBookComponentMixin) listener.getRecipeBookComponent()).getRecipeBookPage();
        RecipeButton recipeButton = ((RecipeBookPageAccessor) recipeBookPage).getButtons().stream().filter(RecipeButton::isHoveredOrFocused).findFirst().orElse(null);
        if(recipeButton != null)
        {
            RecipeBookMenu<?> menu = (RecipeBookMenu<?>) screen.getMenu();
            Slot slot = menu.getSlot(menu.getResultSlotIndex());
            if(menu.getCarried().isEmpty())
            {
                this.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screen.getGuiLeft() + slot.x + 8, screen.getGuiTop() + slot.y + 8);
            }
            else
            {
                this.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screen.getGuiLeft() + slot.x + 8, screen.getGuiTop() + slot.y + 8);
            }
        }
    }

    private void moveMouseToClosestSlot(boolean moving, Screen screen)
    {
        this.nearSlot = false;

        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof AbstractContainerScreen<?> containerScreen)
        {
            /* Prevents cursor from moving until at least some input is detected */
            if(!this.moved)
                return;

            Minecraft mc = Minecraft.getInstance();
            int guiLeft = containerScreen.getGuiLeft();
            int guiTop = containerScreen.getGuiTop();
            int mouseX = (int) (this.mouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth());
            int mouseY = (int) (this.mouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight());

            /* Finds the closest slot in the GUI within 14 pixels (inclusive) */
            Slot closestSlot = null;
            double closestDistance = -1.0;
            for(Slot slot : containerScreen.getMenu().slots)
            {
                int posX = guiLeft + slot.x + 8;
                int posY = guiTop + slot.y + 8;

                double distance = Math.sqrt(Math.pow(posX - mouseX, 2) + Math.pow(posY - mouseY, 2));
                if((closestDistance == -1.0 || distance < closestDistance) && distance <= 14.0)
                {
                    closestSlot = slot;
                    closestDistance = distance;
                }
            }

            if(closestSlot != null && (closestSlot.hasItem() || !containerScreen.getMenu().getCarried().isEmpty()))
            {
                this.nearSlot = true;
                int slotCenterXScaled = guiLeft + closestSlot.x + 8;
                int slotCenterYScaled = guiTop + closestSlot.y + 8;
                int slotCenterX = (int) (slotCenterXScaled / ((double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth()));
                int slotCenterY = (int) (slotCenterYScaled / ((double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight()));
                double deltaX = slotCenterX - this.mouseX;
                double deltaY = slotCenterY - this.mouseY;

                if(!moving)
                {
                    if(mouseX != slotCenterXScaled || mouseY != slotCenterYScaled)
                    {
                        this.mouseX += deltaX * 0.75;
                        this.mouseY += deltaY * 0.75;
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
                this.mouseSpeedX = 0.0F;
                this.mouseSpeedY = 0.0F;
            }
        }
        else
        {
            this.mouseSpeedX = 0.0F;
            this.mouseSpeedY = 0.0F;
        }
    }

    private void setMousePosition(double mouseX, double mouseY)
    {
        if(Config.CLIENT.options.virtualMouse.get())
        {
            this.virtualMouseX = mouseX;
            this.virtualMouseY = mouseY;
        }
        else
        {
            Minecraft mc = Minecraft.getInstance();
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), mouseX, mouseY);
            this.preventReset = true;
        }
    }

    private void handleCreativeScrolling(CreativeModeInventoryScreen creative, Controller controller)
    {
        try
        {
            int i = (creative.getMenu().items.size() + 9 - 1) / 9 - 5;
            int dir = 0;

            if(controller.getRThumbStickYValue() <= -0.8F)
            {
                dir = 1;
            }
            else if(controller.getRThumbStickYValue() >= 0.8F)
            {
                dir = -1;
            }

            Field field = ObfuscationReflectionHelper.findField(CreativeModeInventoryScreen.class, "f_98508_");
            field.setAccessible(true);

            float currentScroll = field.getFloat(creative);
            currentScroll = (float) ((double) currentScroll - (double) dir / (double) i);
            currentScroll = Mth.clamp(currentScroll, 0.0F, 1.0F);
            field.setFloat(creative, currentScroll);
            creative.getMenu().scrollTo(currentScroll);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void handleListScrolling(AbstractSelectionList<?> list, Controller controller)
    {
        double dir = 0;
        float yValue = Config.CLIENT.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
        if(Math.abs(yValue) >= 0.2F)
        {
            this.setControllerInUse();
            dir = yValue;
        }
        dir *= Minecraft.getInstance().getDeltaFrameTime();
        list.setScrollAmount(list.getScrollAmount() + dir * 10);
    }

    private void handleMerchantScrolling(MerchantScreen screen, Controller controller)
    {
        double dir = 0;
        float yValue = Config.CLIENT.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
        if(Math.abs(yValue) >= 0.5F)
        {
            this.setControllerInUse();
            dir = -yValue;
        }
        else
        {
            // Do this to allow thumbstick to be tap up or down
            this.lastMerchantScroll = 0;
        }
        long scrollTime = Util.getMillis();
        if(dir != 0 && scrollTime - this.lastMerchantScroll >= 150)
        {
            screen.mouseScrolled(this.getMouseX(), this.getMouseY(), Math.signum(dir));
            this.lastMerchantScroll = scrollTime;
        }
    }

    private double getMouseX()
    {
        Minecraft mc = Minecraft.getInstance();
        double mouseX = mc.mouseHandler.xpos();
        if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && this.lastUse > 0)
        {
            mouseX = this.virtualMouseX;
        }
        return mouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    private double getMouseY()
    {
        Minecraft mc = Minecraft.getInstance();
        double mouseY = mc.mouseHandler.ypos();
        if(Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && this.lastUse > 0)
        {
            mouseY = this.virtualMouseY;
        }
        return mouseY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
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
        if(screen != null)
        {
            double mouseX = this.getMouseX();
            double mouseY = this.getMouseY();
            this.invokeMouseClick(screen, button, mouseX, mouseY);
        }
    }

    private void invokeMouseClick(Screen screen, int button, double mouseX, double mouseY)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            mc.mouseHandler.activeButton = button;
            mc.mouseHandler.lastMouseEventTime = Blaze3D.getTime();

            Screen.wrapScreenError(() ->
            {
                boolean cancelled = ForgeHooksClient.onScreenMouseClickedPre(screen, mouseX, mouseY, button);
                if(!cancelled)
                {
                    cancelled = screen.mouseClicked(mouseX, mouseY, button);
                    ForgeHooksClient.onScreenMouseClickedPost(screen, mouseX, mouseY, button, cancelled);
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
        if(screen != null)
        {
            double mouseX = this.getMouseX();
            double mouseY = this.getMouseY();
            this.invokeMouseReleased(screen, button, mouseX, mouseY);
        }
    }

    private void invokeMouseReleased(Screen screen, int button, double mouseX, double mouseY)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            mc.mouseHandler.activeButton = -1;

            Screen.wrapScreenError(() ->
            {
                boolean cancelled = ForgeHooksClient.onScreenMouseReleasedPre(screen, mouseX, mouseY, button);
                if(!cancelled)
                {
                    cancelled = screen.mouseReleased(mouseX, mouseY, button);
                    ForgeHooksClient.onScreenMouseReleasedPost(screen, mouseX, mouseY, button, cancelled);
                }
            }, "mouseReleased event handler", screen.getClass().getCanonicalName());
        }
    }

    private enum Navigate
    {
        UP((p, x, y) -> p.getY() < y, (p, v) -> Math.abs(p.getX() - v.x)),
        DOWN((p, x, y) -> p.getY() > y + 1, (p, v) -> Math.abs(p.getX() - v.x)),
        LEFT((p, x, y) -> p.getX() < x, (p, v) -> Math.abs(p.getY() - v.y)),
        RIGHT((p, x, y) -> p.getX() > x + 1, (p, v) -> Math.abs(p.getY() - v.y));

        private NavigatePredicate predicate;
        private BiFunction<? super NavigationPoint, Vector3d, Double> keyExtractor;

        Navigate(NavigatePredicate predicate, BiFunction<? super NavigationPoint, Vector3d, Double> keyExtractor)
        {
            this.predicate = predicate;
            this.keyExtractor = keyExtractor;
        }

        public NavigatePredicate getPredicate()
        {
            return this.predicate;
        }

        public BiFunction<? super NavigationPoint, Vector3d, Double> getKeyExtractor()
        {
            return this.keyExtractor;
        }

        public Comparator<NavigationPoint> getMinComparator(int mouseX, int mouseY)
        {
            return Comparator.comparing(p -> this.keyExtractor.apply(p, new Vector3d(mouseX, mouseY, 0)));
        }

        public static void main(String[] args)
        {
            int slotX = 10;
            int slotY = 20;
            int mouseX = 50;
            int mouseY = 20;
            angle(new SlotNavigationPoint(slotX, slotY, null), mouseX, mouseY, 0);
        }

        private static boolean angle(NavigationPoint point, int mouseX, int mouseY, double offset)
        {
            double angle = Math.toDegrees(Math.atan2(point.getY() - mouseY, point.getX() - mouseX)) + offset;
            return angle > -45 && angle < 45;
        }
    }

    private interface NavigatePredicate
    {
        boolean test(NavigationPoint point, int mouseX, int mouseY);
    }
}
