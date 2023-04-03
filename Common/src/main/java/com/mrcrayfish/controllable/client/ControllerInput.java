package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.navigation.ListEntryNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListWidgetNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.EventHelper;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.controllable.mixin.client.OverlayRecipeComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.framework.api.event.ClientEvents;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

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
    private static final ResourceLocation CURSOR_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/cursor.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");

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

    public ControllerInput()
    {
        TickEvents.START_CLIENT.register(this::onClientTick);
        TickEvents.START_CLIENT.register(this::onClientTickStart);
        TickEvents.END_RENDER.register(this::onRenderTickEnd);
        ScreenEvents.OPENED.register(this::onScreenOpened);
        ScreenEvents.BEFORE_DRAW.register(this::onScreenRenderPre);
        ScreenEvents.AFTER_DRAW.register(this::drawVirtualCursor);
        ClientEvents.PLAYER_INPUT_UPDATE.register(this::onInputUpdate);
    }

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

    private void onClientTick()
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

        float deadZone = (float) Math.min(1.0F, Config.CLIENT.client.options.deadZone.get() + 0.25F);

        /* Only need to run code if left thumb stick has input */
        boolean lastMoving = this.moving;
        float xAxis = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickXValue() : controller.getRThumbStickXValue();
        float yAxis = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickYValue() : controller.getRThumbStickYValue();
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
                if(Controllable.getController() != null && Config.CLIENT.client.options.virtualMouse.get())
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
            double mouseSpeed = Config.CLIENT.client.options.mouseSpeed.get() * mc.getWindow().getGuiScale();

            // When hovering over slots, slows down the mouse speed to make it easier
            if(mc.screen instanceof AbstractContainerScreen<?> screen)
            {
                if(ClientServices.CLIENT.getSlotUnderMouse(screen) != null)
                {
                    mouseSpeed *= Config.CLIENT.client.options.hoverModifier.get();
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
                    eventListeners.add(((RecipeBookComponentAccessor) recipeBook).getFilterButton());
                    eventListeners.addAll(((RecipeBookComponentAccessor) recipeBook).getRecipeTabs());
                    RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) recipeBook).getRecipeBookPage();
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
                    int rowTop = ClientServices.CLIENT.getAbstractListRowTop(list, i);
                    int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(list, i);
                    int listTop = ClientServices.CLIENT.getAbstractListTop(list);
                    int listBottom = ClientServices.CLIENT.getAbstractListBottom(list);
                    if(rowTop < listTop && rowBottom > listBottom) // Is visible
                        continue;

                    Object entry = list.children().get(i);
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
                mouseSpeed *= Config.CLIENT.client.options.hoverModifier.get();
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

        if(Config.CLIENT.client.options.virtualMouse.get() && (this.mouseX != this.prevMouseX || this.mouseY != this.prevMouseY))
        {
            this.performMouseDrag(this.virtualMouseX, this.virtualMouseY, this.mouseX - this.prevMouseX, this.mouseY - this.prevMouseY);
        }
    }

    private void onScreenOpened(Screen screen)
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

    private void onScreenRenderPre(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick)
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
                double mX = (this.prevMouseX + (this.mouseX - this.prevMouseX) * partialTicks + 0.5);
                double mY = (this.prevMouseY + (this.mouseY - this.prevMouseY) * partialTicks + 0.5);
                this.setMousePosition(mX, mY);
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
                    int activeMouseButton = ClientServices.CLIENT.getActiveMouseButton();
                    double lastMouseEventTime = ClientServices.CLIENT.getLastMouseEventTime();
                    if(activeMouseButton != -1 && lastMouseEventTime > 0.0D)
                    {
                        ClientServices.CLIENT.sendMouseDrag(screen, dragX, dragY, finalMouseX, finalMouseY, activeMouseButton);
                    }
                }
            }
        }
    }

    public void drawVirtualCursor(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualMouse.get() && this.lastUse > 0)
        {
            poseStack.pushPose();
            CursorType type = Config.CLIENT.client.options.cursorType.get();
            Minecraft mc = Minecraft.getInstance();
            if(mc.player == null || (mc.player.inventoryMenu.getCarried().isEmpty() || type == CursorType.CONSOLE))
            {
                double guiScale = mc.getWindow().getGuiScale();
                double virtualCursorX = (this.prevMouseX + (this.mouseX - this.prevMouseX) * mc.getFrameTime());
                double virtualCursorY = (this.prevMouseY + (this.mouseY - this.prevMouseY) * mc.getFrameTime());
                poseStack.translate(virtualCursorX / guiScale, virtualCursorY / guiScale, 500);
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

    private void onRenderTickEnd(float partialTick)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
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
                player.turn((this.targetYaw / 0.15) * (Config.CLIENT.client.options.invertRotation.get() ? -1 : 1) * elapsedTicks, (this.targetPitch / 0.15) * (Config.CLIENT.client.options.invertLook.get() ? -1 : 1) * elapsedTicks);
            }
            if(player.getVehicle() != null)
            {
                player.getVehicle().onPassengerTurned(player);
            }
        }
    }

    private void onClientTickStart()
    {
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
            float deadZone = Config.CLIENT.client.options.deadZone.get().floatValue();
            float pitchSensitivity = Config.CLIENT.client.options.pitchSensitivity.get().floatValue();
            float yawSensitivity = Config.CLIENT.client.options.yawSensitivity.get().floatValue();
            double rotationSpeed = Config.CLIENT.client.options.rotationSpeed.get();
            boolean rightXThumbstickMoved = Math.abs(controller.getRThumbStickXValue()) >= deadZone;
            boolean rightYThumbstickMoved = Math.abs(controller.getRThumbStickYValue()) >= deadZone;
            if(rightXThumbstickMoved || rightYThumbstickMoved)
            {
                this.setControllerInUse();
                Value<Float> yawSpeed = new Value<>((float) rotationSpeed * yawSensitivity);
                Value<Float> pitchSpeed = new Value<>((float) rotationSpeed * pitchSensitivity);
                boolean cancelled = ControllerEvents.UPDATE_CAMERA.post().handle(yawSpeed, pitchSpeed);
                if(!cancelled)
                {
                    cancelled = ClientServices.CLIENT.sendLegacyControllerEventTurn(controller, yawSpeed, pitchSpeed);
                }
                if(!cancelled)
                {
                    if(rightXThumbstickMoved)
                    {
                        float deadZoneTrimX = (controller.getRThumbStickXValue() > 0 ? 1 : -1) * deadZone;
                        this.targetYaw = (yawSpeed.get() * (controller.getRThumbStickXValue() - deadZoneTrimX) / (1.0F - deadZone)) * 0.33F;
                    }
                    if(rightYThumbstickMoved)
                    {
                        float deadZoneTrimY = (controller.getRThumbStickYValue() > 0 ? 1 : -1) * deadZone;
                        this.targetPitch = (pitchSpeed.get() * (controller.getRThumbStickYValue() - deadZoneTrimY) / (1.0F - deadZone)) * 0.33F;
                    }
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

    private void onInputUpdate(Player p, Input input)
    {
        LocalPlayer player = (LocalPlayer) p;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(this.keyboardSneaking && !mc.options.keyShift.isDown())
        {
            this.sneaking = false;
            this.keyboardSneaking = false;
        }

        if(!mc.options.toggleCrouch().get())
        {
            this.sneaking = ButtonBindings.SNEAK.isButtonDown();
        }

        if(mc.options.keyShift.isDown())
        {
            this.sneaking = true;
            this.keyboardSneaking = true;
        }

        if(player.getAbilities().flying || player.isPassenger())
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

        input.shiftKeyDown = this.sneaking;

        if(mc.screen == null)
        {
            if((!RadialMenuHandler.instance().isVisible() || Config.CLIENT.client.options.radialThumbstick.get() != Thumbstick.LEFT) && !EventHelper.postMoveEvent(controller))
            {
                float deadZone = Config.CLIENT.client.options.deadZone.get().floatValue();
                float sneakBonus = player.isMovingSlowly() ? Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(player), 0.0F, 1.0F) : 1.0F;

                if(Math.abs(controller.getLThumbStickYValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickYValue() > 0.0F ? -1 : 1;
                    input.up = dir > 0;
                    input.down = dir < 0;
                    input.forwardImpulse = dir * Mth.clamp((Math.abs(controller.getLThumbStickYValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);
                    input.forwardImpulse *= sneakBonus;
                }

                if(player.getVehicle() instanceof Boat)
                {
                    deadZone = 0.5F;
                }

                if(Math.abs(controller.getLThumbStickXValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    input.right = dir < 0;
                    input.left = dir > 0;
                    input.leftImpulse = dir * Mth.clamp((Math.abs(controller.getLThumbStickXValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);
                    input.leftImpulse *= sneakBonus;
                }
            }

            if(this.ignoreInput && !ButtonBindings.JUMP.isButtonDown())
            {
                this.ignoreInput = false;
            }

            if(ButtonBindings.JUMP.isButtonDown() && !this.ignoreInput)
            {
                input.jumping = true;
            }
        }

        int rightClickDelay = ClientServices.CLIENT.getRightClickDelay(mc);
        if(ButtonBindings.USE_ITEM.isButtonDown() && rightClickDelay == 0 && !player.isUsingItem())
        {
            ClientServices.CLIENT.startUseItem(mc);
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
            Value<Integer> newButton = new Value<>(button);
            boolean cancelled = ControllerEvents.INPUT.post().handle(controller, newButton, button, state);
            if(!cancelled)
            {
                cancelled = ClientServices.CLIENT.sendLegacyControllerEventButtonInput(controller, newButton, button, state);
            }
            if(cancelled)
            {
                return;
            }

            button = newButton.get();
            ButtonBinding.setButtonState(button, state);
        }

        boolean cancelled = ControllerEvents.BUTTON.post().handle(controller);
        if(!cancelled)
        {
            cancelled = ClientServices.CLIENT.sendLegacyControllerEventButton(controller);
        }
        if(cancelled)
        {
            return;
        }

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
                if(ButtonBindings.OPEN_INVENTORY.isButtonPressed() && mc.gameMode != null && mc.player != null)
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
                        LocalPlayer player = mc.player;
                        boolean canSprint = !player.isSprinting() && !player.hasEffect(MobEffects.BLINDNESS);
                        boolean hasRequiredFood = (float) player.getFoodData().getFoodLevel() > 6.0F || player.getAbilities().mayfly;
                        boolean hasImpulse = player.isUnderWater() ? player.input.hasForwardImpulse() : (double) player.input.forwardImpulse >= 0.8D;
                        boolean canSwimInFluid = ClientServices.CLIENT.canLocalPlayerSwimInFluid(player);
                        boolean usingItem = player.isUsingItem();
                        if(canSprint && canSwimInFluid && hasImpulse && hasRequiredFood && !usingItem)
                        {
                            player.setSprinting(true);
                        }
                    }
                }
                else if(ButtonBindings.SNEAK.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.getAbilities().flying && !this.isFlying && !mc.player.isPassenger())
                    {
                        if(mc.options.toggleCrouch().get())
                        {
                            this.sneaking = !this.sneaking;
                            if(!this.sneaking && mc.options.keyShift.isDown())
                            {
                                this.keyboardSneaking = false;
                                mc.options.keyShift.setDown(true);
                            }
                            else if(this.sneaking && !mc.options.keyShift.isDown())
                            {
                                this.keyboardSneaking = true;
                                mc.options.keyShift.setDown(true);
                            }
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
                            ClientServices.CLIENT.startAttack(mc);
                        }
                        else if(ButtonBindings.USE_ITEM.isButtonPressed())
                        {
                            ClientServices.CLIENT.startUseItem(mc);
                        }
                        else if(ButtonBindings.PICK_BLOCK.isButtonPressed())
                        {
                            ClientServices.CLIENT.pickBlock(mc);
                        }
                    }
                }
            }
            else
            {
                if(ButtonBindings.CLOSE_INVENTORY.isButtonPressed())
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
                else if(ButtonBindings.TOGGLE_CRAFT_BOOK.isButtonPressed())
                {
                    if(mc.screen instanceof RecipeUpdateListener listener)
                    {
                        // Since no reference to craft book button, instead search for it and invoke press.
                        ClientServices.CLIENT.getScreenRenderables(mc.screen).stream().filter(widget -> {
                            return widget instanceof ImageButton btn && RECIPE_BUTTON_LOCATION.equals(ClientServices.CLIENT.getImageButtonResource(btn));
                        }).findFirst().ifPresent(btn -> ((Button) btn).onPress());
                        boolean visible = listener.getRecipeBookComponent().isVisible();
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, visible ? 1.0F : 0.95F));
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

                    if(Config.CLIENT.client.options.quickCraft.get())
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
        ClientServices.CLIENT.scrollCreativeTabs(screen, dir);
    }

    private void scrollRecipeTab(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookComponentAccessor recipeBookMixin = ((RecipeBookComponentAccessor) recipeBook);
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
        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookComponentAccessor) recipeBook).getRecipeBookPage();
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
        ControllerEvents.GATHER_NAVIGATION_POINTS.post().handle(points);
        points.addAll(ClientServices.CLIENT.sendLegacyGatherNavigationPoints());

        // Get only the points that are in the target direction
        points.removeIf(p -> !navigate.getPredicate().test(p, mouseX, mouseY));
        if(points.isEmpty())
            return;

        Vector3d mousePos = new Vector3d(mouseX, mouseY, 0);
        Optional<NavigationPoint> minimumPointOptional = points.stream().min(navigate.getMinComparator(mouseX, mouseY));
        if(minimumPointOptional.isEmpty())
            return;

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
                double lastTargetX = this.mouseX;
                double lastTargetY = this.mouseY;
                this.mouseX = this.prevMouseX = screenX;
                this.mouseY = this.prevMouseY = screenY;
                this.setMousePosition(screenX, screenY);
                if(Config.CLIENT.client.options.uiSounds.get())
                {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 2.0F));
                }
                this.performMouseDrag(this.mouseX, this.mouseY, screenX - lastTargetX, screenY - lastTargetY);
            });
        }
    }

    private List<NavigationPoint> gatherNavigationPoints(Screen screen, Navigate navigate, int mouseX, int mouseY)
    {
        List<NavigationPoint> points = new ArrayList<>();

        if(screen instanceof AbstractContainerScreen<?> containerScreen)
        {
            int guiLeft = ClientServices.CLIENT.getScreenLeft(containerScreen);
            int guiTop = ClientServices.CLIENT.getScreenTop(containerScreen);
            for(Slot slot : containerScreen.getMenu().slots)
            {
                if(ClientServices.CLIENT.getSlotUnderMouse(containerScreen) == slot)
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
                int itemHeight = ClientServices.CLIENT.getListItemHeight(list);
                for(int i = 0; i < count; i++)
                {
                    GuiEventListener entry = list.children().get(i);
                    int rowTop = ClientServices.CLIENT.getAbstractListRowTop(list, i);
                    int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(list, i);
                    int listTop = ClientServices.CLIENT.getAbstractListTop(list);
                    int listBottom = ClientServices.CLIENT.getAbstractListBottom(list);
                    if(rowTop > listTop - itemHeight && rowBottom < listBottom + itemHeight)
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
                widgets.add(((RecipeBookComponentAccessor) recipeBook).getFilterButton());
                widgets.addAll(((RecipeBookComponentAccessor) recipeBook).getRecipeTabs());

                RecipeBookPage page = ((RecipeBookComponentAccessor) recipeBook).getRecipeBookPage();
                OverlayRecipeComponent overlay = ((RecipeBookPageAccessor) page).getOverlay();
                if(overlay.isVisible())
                {
                    widgets.addAll(((OverlayRecipeComponentAccessor) overlay).getRecipeButtons());
                }
                else
                {
                    RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) recipeBook).getRecipeBookPage();
                    widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).getButtons());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).getForwardButton());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).getBackButton());
                }
            }
        }

        for(AbstractWidget widget : widgets)
        {
            if(widget == null || widget.isHoveredOrFocused() || !widget.visible || !widget.active)
                continue;
            int posX = widget.getX() + widget.getWidth() / 2;
            int posY = widget.getY() + widget.getHeight() / 2;
            points.add(new WidgetNavigationPoint(posX, posY, widget));
        }

        if(screen instanceof CreativeModeInventoryScreen creativeScreen)
        {
            ClientServices.CLIENT.gatherCreativeTabNavigationPoints(creativeScreen, points);
        }

        if(Controllable.isJeiLoaded() && ClientHelper.isPlayingGame())
        {
            points.addAll(ClientServices.CLIENT.getJeiNavigationPoints());
        }

        return points;
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

        RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) listener.getRecipeBookComponent()).getRecipeBookPage();
        RecipeButton recipeButton = ((RecipeBookPageAccessor) recipeBookPage).getButtons().stream().filter(RecipeButton::isHoveredOrFocused).findFirst().orElse(null);
        if(recipeButton != null)
        {
            RecipeBookMenu<?> menu = (RecipeBookMenu<?>) screen.getMenu();
            Slot slot = menu.getSlot(menu.getResultSlotIndex());
            int screenLeft = ClientServices.CLIENT.getScreenLeft(screen);
            int screenTop = ClientServices.CLIENT.getScreenTop(screen);
            if(menu.getCarried().isEmpty())
            {
                this.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
            }
            else
            {
                this.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
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
            int guiLeft = ClientServices.CLIENT.getScreenLeft(containerScreen);
            int guiTop = ClientServices.CLIENT.getScreenTop(containerScreen);
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
        if(Config.CLIENT.client.options.virtualMouse.get())
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

    private void handleCreativeScrolling(CreativeModeInventoryScreen screen, Controller controller)
    {
        int i = (screen.getMenu().items.size() + 9 - 1) / 9 - 5;
        int dir = 0;

        if(controller.getRThumbStickYValue() <= -0.8F)
        {
            dir = 1;
        }
        else if(controller.getRThumbStickYValue() >= 0.8F)
        {
            dir = -1;
        }

        float currentScroll = ClientServices.CLIENT.getCreativeScrollOffset(screen);
        currentScroll = (float) ((double) currentScroll - (double) dir / (double) i);
        currentScroll = Mth.clamp(currentScroll, 0.0F, 1.0F);
        ClientServices.CLIENT.setCreativeScrollOffset(screen, currentScroll);
        screen.getMenu().scrollTo(currentScroll);
    }

    private void handleListScrolling(AbstractSelectionList<?> list, Controller controller)
    {
        double dir = 0;
        float yValue = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
        if(Math.abs(yValue) >= 0.2F)
        {
            this.setControllerInUse();
            dir = yValue;
        }
        dir *= Minecraft.getInstance().getDeltaFrameTime();
        list.setScrollAmount(list.getScrollAmount() + dir * Config.CLIENT.client.options.listScrollSpeed.get());
    }

    private void handleMerchantScrolling(MerchantScreen screen, Controller controller)
    {
        double dir = 0;
        float yValue = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
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
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualMouse.get() && this.lastUse > 0)
        {
            mouseX = this.virtualMouseX;
        }
        return mouseX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    private double getMouseY()
    {
        Minecraft mc = Minecraft.getInstance();
        double mouseY = mc.mouseHandler.ypos();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualMouse.get() && this.lastUse > 0)
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
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(button);
            ClientServices.CLIENT.setLastMouseEventTime(Blaze3D.getTime());
            ClientServices.CLIENT.sendScreenMouseClickPre(screen, mouseX, mouseY, button);
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
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(-1);
            ClientServices.CLIENT.sendScreenMouseReleasedPre(screen, mouseX, mouseY, button);
        }
    }

    private enum Navigate
    {
        UP((p, x, y) -> p.getY() < y, (p, v) -> Math.abs(p.getX() - v.x)),
        DOWN((p, x, y) -> p.getY() > y + 1, (p, v) -> Math.abs(p.getX() - v.x)),
        LEFT((p, x, y) -> p.getX() < x, (p, v) -> Math.abs(p.getY() - v.y)),
        RIGHT((p, x, y) -> p.getX() > x + 1, (p, v) -> Math.abs(p.getY() - v.y));

        private final NavigatePredicate predicate;
        private final BiFunction<? super NavigationPoint, Vector3d, Double> keyExtractor;

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
    }

    private interface NavigatePredicate
    {
        boolean test(NavigationPoint point, int mouseX, int mouseY);
    }
}
