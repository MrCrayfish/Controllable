package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListEntryNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListWidgetNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.EventHelper;
import com.mrcrayfish.controllable.client.util.ScreenUtil;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.controllable.mixin.client.OverlayRecipeComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.controllable.platform.Services;
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
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
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
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

//TODO this has become a big mess, and really needs to be organised

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
    private double virtualCursorX;
    private double virtualCursorY;
    private int prevCursorX;
    private int prevCursorY;
    private int cursorX;
    private int cursorY;
    private double cursorSpeedX;
    private double cursorSpeedY;
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

    public double getVirtualCursorX()
    {
        return this.virtualCursorX;
    }

    public double getVirtualCursorY()
    {
        return this.virtualCursorY;
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
        this.prevCursorX = this.cursorX;
        this.prevCursorY = this.cursorY;

        if(this.lastUse > 0)
        {
            this.lastUse--;
        }

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        /* If the player is mining a block, actively mark the controller as "in use" */
        if((Math.abs(controller.getLTriggerValue()) > 0.0F || Math.abs(controller.getRTriggerValue()) > 0.0F) && !(Minecraft.getInstance().screen instanceof ControllerLayoutScreen))
        {
            this.setControllerInUse();
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null || mc.screen instanceof ControllerLayoutScreen)
            return;

        /* Only need to run code if left thumb stick has input */
        float threshold = 0.35F;
        boolean lastMoving = this.moving;
        float inputX = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickXValue() : controller.getRThumbStickXValue();
        float inputY = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getLThumbStickYValue() : controller.getRThumbStickYValue();
        this.moving = Math.abs(inputX) >= threshold || Math.abs(inputY) >= threshold;
        if(this.moving)
        {
            /* Updates the target mouse position when the initial thumb stick movement is
             * detected. This fixes an issue when the user moves the cursor with the mouse then
             * switching back to controller, the cursor would jump to old target mouse position. */
            if(!lastMoving)
            {
                double cursorX = mc.mouseHandler.xpos();
                double cursorY = mc.mouseHandler.ypos();
                if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get())
                {
                    cursorX = this.virtualCursorX;
                    cursorY = this.virtualCursorY;
                }
                this.prevCursorX = this.cursorX = (int) cursorX;
                this.prevCursorY = this.cursorY = (int) cursorY;
            }

            /* Update the speed of the cursor */
            this.cursorSpeedX = Math.abs(inputX) >= threshold ? ClientHelper.applyDeadzone(inputX, threshold) : 0.0F;
            this.cursorSpeedY = Math.abs(inputY) >= threshold ? ClientHelper.applyDeadzone(inputY, threshold) : 0.0F;

            /* Mark the controller as in use because the cursor is moving */
            this.setControllerInUse();
        }

        if(this.lastUse <= 0)
        {
            this.cursorSpeedX = 0F;
            this.cursorSpeedY = 0F;
            return;
        }

        if(Math.abs(this.cursorSpeedX) > 0F || Math.abs(this.cursorSpeedY) > 0F)
        {
            double cursorSpeed = Config.CLIENT.client.options.cursorSpeed.get() * mc.getWindow().getGuiScale();

            // When hovering over slots, slows down the mouse speed to make it easier
            if(mc.screen instanceof AbstractContainerScreen<?> screen)
            {
                if(ClientServices.CLIENT.getSlotUnderMouse(screen) != null)
                {
                    cursorSpeed *= Config.CLIENT.client.options.hoverModifier.get();
                }
            }

            double cursorX = this.virtualCursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double cursorY = this.virtualCursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            List<GuiEventListener> eventListeners = new ArrayList<>(mc.screen.children());
            if(mc.screen instanceof RecipeUpdateListener)
            {
                RecipeBookComponent recipeBook = ((RecipeUpdateListener) mc.screen).getRecipeBookComponent();
                if(recipeBook.isVisible())
                {
                    eventListeners.add(((RecipeBookComponentAccessor) recipeBook).controllableGetFilterButton());
                    eventListeners.addAll(((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeTabs());
                    RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
                    eventListeners.addAll(((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons());
                    eventListeners.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetForwardButton());
                    eventListeners.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetBackButton());
                }
            }

            GuiEventListener hoveredListener = eventListeners.stream().filter(o -> o != null && o.isMouseOver(cursorX, cursorY)).findFirst().orElse(null);
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

                    GuiEventListener hovered = handler.children().stream().filter(o -> o != null && o.isMouseOver(cursorX, cursorY)).findFirst().orElse(null);
                    if(hovered == null)
                        continue;

                    hoveredListener = hovered;
                    break;
                }
            }
            if(hoveredListener != null)
            {
                cursorSpeed *= Config.CLIENT.client.options.hoverModifier.get();
            }

            this.cursorX += cursorSpeed * this.cursorSpeedX;
            this.cursorX = Mth.clamp(this.cursorX, 0, mc.getWindow().getWidth());
            this.cursorY += cursorSpeed * this.cursorSpeedY;
            this.cursorY = Mth.clamp(this.cursorY, 0, mc.getWindow().getHeight());
            this.setControllerInUse();
            this.moved = true;
        }

        this.moveCursorToClosestSlot(this.moving, mc.screen);

        if(mc.screen instanceof CreativeModeInventoryScreen)
        {
            this.handleCreativeScrolling((CreativeModeInventoryScreen) mc.screen, controller);
        }

        if(Config.CLIENT.client.options.virtualCursor.get() && (this.cursorX != this.prevCursorX || this.cursorY != this.prevCursorY))
        {
            this.performMouseDrag(this.virtualCursorX, this.virtualCursorY, this.cursorX - this.prevCursorX, this.cursorY - this.prevCursorY);
        }
    }

    private void onScreenOpened(Screen screen)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null)
        {
            this.nearSlot = false;
            this.moved = false;
            this.cursorSpeedX = 0.0;
            this.cursorSpeedY = 0.0;
            this.virtualCursorX = this.cursorX = this.prevCursorX = (int) (mc.getWindow().getWidth() / 2F);
            this.virtualCursorY = this.cursorY = this.prevCursorY = (int) (mc.getWindow().getHeight() / 2F);
        }
    }

    private void onScreenRenderPre(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null && (this.cursorX != this.prevCursorX || this.cursorY != this.prevCursorY))
        {
            if(!(mc.screen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getInstance().getFrameTime();
                double renderCursorX = (this.prevCursorX + (this.cursorX - this.prevCursorX) * partialTicks + 0.5);
                double renderCursorY = (this.prevCursorY + (this.cursorY - this.prevCursorY) * partialTicks + 0.5);
                this.setCursorPosition(renderCursorX, renderCursorY);
            }
        }
    }

    private void performMouseDrag(double cursorX, double cursorY, double dragX, double dragY)
    {
        if(Controllable.getController() != null)
        {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            if(screen != null)
            {
                if(mc.getOverlay() == null)
                {
                    double finalCursorX = cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
                    double finalCursorY = cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
                    Screen.wrapScreenError(() -> screen.mouseMoved(finalCursorX, finalCursorY), "mouseMoved event handler", ((GuiEventListener) screen).getClass().getCanonicalName());
                    int activeMouseButton = ClientServices.CLIENT.getActiveMouseButton();
                    double lastMouseEventTime = ClientServices.CLIENT.getLastMouseEventTime();
                    if(activeMouseButton != -1 && lastMouseEventTime > 0.0D)
                    {
                        ClientServices.CLIENT.sendMouseDrag(screen, dragX, dragY, finalCursorX, finalCursorY, activeMouseButton);
                    }
                }
            }
        }
    }

    public void drawVirtualCursor(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && this.lastUse > 0)
        {
            poseStack.pushPose();
            CursorType type = Config.CLIENT.client.options.cursorType.get();
            Minecraft mc = Minecraft.getInstance();
            if(mc.player == null || (mc.player.inventoryMenu.getCarried().isEmpty() || type == CursorType.CONSOLE))
            {
                double guiScale = mc.getWindow().getGuiScale();
                double virtualCursorX = (this.prevCursorX + (this.cursorX - this.prevCursorX) * mc.getFrameTime());
                double virtualCursorY = (this.prevCursorY + (this.cursorY - this.prevCursorY) * mc.getFrameTime());
                double zIndex = Services.PLATFORM.isForge() ? 500 : 3000; // Hack until I make Forge/Fabric calls the same
                poseStack.translate(virtualCursorX / guiScale, virtualCursorY / guiScale, zIndex);
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
        double cursorX = this.virtualCursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double cursorY = this.virtualCursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        if(mc.screen != null && this.lastUse > 0)
        {
            if(mc.screen instanceof MerchantScreen screen)
            {
                this.handleMerchantScrolling(screen, controller);
                return;
            }

            float yValue = Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
            if(Math.abs(yValue) >= 0.2F)
            {
                GuiEventListener hoveredListener = ScreenUtil.findHoveredListener(mc.screen, cursorX, cursorY, listener -> listener instanceof AbstractSelectionList<?>).orElse(null);
                if(hoveredListener instanceof AbstractSelectionList<?> selectionList)
                {
                    this.handleListScrolling(selectionList, controller);
                }
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
            float inputX = controller.getRThumbStickXValue();
            float inputY = controller.getRThumbStickYValue();
            boolean canMoveHorizontally = Math.abs(inputX) > 0;
            boolean canMoveVertically = Math.abs(inputY) > 0;
            if(canMoveHorizontally || canMoveVertically)
            {
                float pitchSensitivity = Config.CLIENT.client.options.pitchSensitivity.get().floatValue();
                float yawSensitivity = Config.CLIENT.client.options.yawSensitivity.get().floatValue();
                float rotationSpeed = Config.CLIENT.client.options.rotationSpeed.get().floatValue();
                float spyglassSensitivity = player.isScoping() ? Config.CLIENT.client.options.spyglassSensitivity.get().floatValue() : 1.0F;

                Value<Float> yawSpeed = new Value<>(rotationSpeed * yawSensitivity * spyglassSensitivity);
                Value<Float> pitchSpeed = new Value<>(rotationSpeed * pitchSensitivity * spyglassSensitivity);
                if(!EventHelper.postUpdateCameraEvent(controller, yawSpeed, pitchSpeed))
                {
                    if(canMoveHorizontally)
                    {
                        this.targetYaw = yawSpeed.get() * inputX * 0.33F;
                    }
                    if(canMoveVertically)
                    {
                        this.targetPitch = pitchSpeed.get() * inputY * 0.33F;
                    }
                }

                /* Mark the controller as in use because the camera is turning */
                this.setControllerInUse();
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
                float sneakBonus = player.isMovingSlowly() ? Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(player), 0.0F, 1.0F) : 1.0F;
                float inputX = controller.getLThumbStickXValue();
                float inputY = controller.getLThumbStickYValue();

                if(Math.abs(inputY) > 0)
                {
                    input.up = inputY < 0;
                    input.down = inputY > 0;
                    input.forwardImpulse = -inputY;
                    input.forwardImpulse *= sneakBonus;
                    this.setControllerInUse();
                }

                float threshold = player.getVehicle() instanceof Boat ? 0.5F : 0.0F;
                if(Math.abs(inputX) > threshold)
                {
                    input.right = inputX > 0;
                    input.left = inputX < 0;
                    input.leftImpulse = -inputX;
                    input.leftImpulse *= sneakBonus;
                    this.setControllerInUse();
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
            if(EventHelper.postInputEvent(controller, newButton, button, state))
                return;

            button = newButton.get();
            ButtonBinding.setButtonState(button, state);
        }

        if(EventHelper.postButtonEvent(controller))
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
                    this.cycleThirdPersonView();
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
                    if(ButtonBindings.OPEN_CONTROLLABLE_SETTINGS.isButtonPressed())
                    {
                        mc.setScreen(new SettingsScreen(null, 1));
                        return;
                    }
                    else if(ButtonBindings.OPEN_CHAT.isButtonPressed())
                    {
                        ClientServices.CLIENT.openChatScreen("");
                        return;
                    }

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
                        /*else if(ButtonBindings.USE_ITEM.isButtonPressed())
                        {
                            ClientServices.CLIENT.startUseItem(mc);
                        }*/
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
                    if(mc.screen.children().stream().anyMatch(listener -> listener instanceof TabNavigationBar))
                    {
                        this.navigateTabBar(mc.screen, 1);
                    }
                    else if(mc.screen instanceof CreativeModeInventoryScreen)
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
                    if(mc.screen.children().stream().anyMatch(listener -> listener instanceof TabNavigationBar))
                    {
                        this.navigateTabBar(mc.screen, -1);
                    }
                    else if(mc.screen instanceof CreativeModeInventoryScreen)
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
                    this.navigateCursor(mc.screen, Navigate.UP);
                }
                else if(ButtonBindings.NAVIGATE_DOWN.isButtonPressed())
                {
                    this.navigateCursor(mc.screen, Navigate.DOWN);
                }
                else if(ButtonBindings.NAVIGATE_LEFT.isButtonPressed())
                {
                    this.navigateCursor(mc.screen, Navigate.LEFT);
                }
                else if(ButtonBindings.NAVIGATE_RIGHT.isButtonPressed())
                {
                    this.navigateCursor(mc.screen, Navigate.RIGHT);
                }
                else if(button == ButtonBindings.PICKUP_ITEM.getButton())
                {
                    this.invokeMouseClick(mc.screen, 0);

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
                    this.invokeMouseClick(mc.screen, 1);
                }
                else if(button == ButtonBindings.QUICK_MOVE.getButton() && mc.player != null)
                {
                    if(mc.player.inventoryMenu.getCarried().isEmpty())
                    {
                        this.invokeMouseClick(mc.screen, 0);
                    }
                    else
                    {
                        this.invokeMouseReleased(mc.screen, 1);
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
                    this.invokeMouseReleased(mc.screen, 0);
                }
                else if(button == ButtonBindings.SPLIT_STACK.getButton())
                {
                    this.invokeMouseReleased(mc.screen, 1);
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
        RecipeBookTabButton currentTab = recipeBookMixin.controllableGetCurrentTab();
        List<RecipeBookTabButton> tabs = recipeBookMixin.controllableGetRecipeTabs();
        int nextTabIndex = tabs.indexOf(currentTab) + dir;
        if(nextTabIndex >= 0 && nextTabIndex < tabs.size())
        {
            RecipeBookTabButton newTab = tabs.get(nextTabIndex);
            currentTab.setStateTriggered(false);
            recipeBookMixin.controllableSetCurrentTab(newTab);
            newTab.setStateTriggered(true);
            recipeBookMixin.controllableUpdateCollections(true);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void scrollRecipePage(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
        if(dir > 0 && page.controllableGetForwardButton().visible || dir < 0 && page.controllableGetBackButton().visible)
        {
            int currentPage = page.controllableGetCurrentPage();
            page.controllableSetCurrentPage(currentPage + dir);
            page.controllableUpdateButtonsForPage();
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void navigateTabBar(Screen screen, int dir)
    {
        TabNavigationBar bar = screen.children().stream().filter(listener -> listener instanceof TabNavigationBar).map(listener -> (TabNavigationBar) listener).findFirst().orElse(null);
        if(bar != null)
        {
            List<TabButton> buttons = new ArrayList<>();
            bar.children().forEach(listener ->
            {
                if(listener instanceof TabButton button)
                {
                    buttons.add(button);
                }
            });
            int selectedIndex = buttons.stream().filter(TabButton::isSelected).map(buttons::indexOf).findFirst().orElse(-1);
            if(selectedIndex != -1)
            {
                int newIndex = selectedIndex + dir;
                if(newIndex >= 0 && newIndex < buttons.size())
                {
                    bar.selectTab(newIndex, true);
                }
            }
        }
    }

    private void navigateCursor(Screen screen, Navigate navigate)
    {
        Minecraft mc = Minecraft.getInstance();
        int cursorX = (int) (this.cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth());
        int cursorY = (int) (this.cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight());

        List<NavigationPoint> points = this.gatherNavigationPoints(screen, navigate, cursorX, cursorY);

        // Gather any extra navigation points from event
        ControllerEvents.GATHER_NAVIGATION_POINTS.post().handle(points);
        points.addAll(ClientServices.CLIENT.sendLegacyGatherNavigationPoints());

        // Get only the points that are in the target direction
        points.removeIf(p -> !navigate.getPredicate().test(p, cursorX, cursorY));
        if(points.isEmpty())
            return;

        Vector3d cursorVec = new Vector3d(cursorX, cursorY, 0);
        Optional<NavigationPoint> minimumPointOptional = points.stream().min(navigate.getMinComparator(cursorX, cursorY));
        if(minimumPointOptional.isEmpty())
            return;

        double minimumDelta = navigate.getKeyExtractor().apply(minimumPointOptional.get(), cursorVec) + 10;
        Optional<NavigationPoint> targetPointOptional = points.stream().filter(point -> navigate.getKeyExtractor().apply(point, cursorVec) <= minimumDelta).min(Comparator.comparing(p -> p.distanceTo(cursorX, cursorY)));
        if(targetPointOptional.isPresent())
        {
            NavigationPoint targetPoint = targetPointOptional.get();
            targetPoint.onNavigate();
            mc.tell(() -> // Run next frame to allow lists to update widget positions
            {
                this.performMouseDrag(this.cursorX, this.cursorY, 0, 0);
                int screenX = (int) (targetPoint.getX() / ((double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth()));
                int screenY = (int) (targetPoint.getY() / ((double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight()));
                double lastTargetX = this.cursorX;
                double lastTargetY = this.cursorY;
                this.cursorX = this.prevCursorX = screenX;
                this.cursorY = this.prevCursorY = screenY;
                this.setCursorPosition(screenX, screenY);
                if(Config.CLIENT.client.options.uiSounds.get())
                {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 2.0F));
                }
                this.performMouseDrag(this.cursorX, this.cursorY, screenX - lastTargetX, screenY - lastTargetY);
            });
        }
    }

    private List<NavigationPoint> gatherNavigationPoints(Screen screen, Navigate navigate, int cursorX, int cursorY)
    {
        List<NavigationPoint> points = new ArrayList<>();
        List<AbstractWidget> widgets = new ArrayList<>();

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

        for(GuiEventListener listener : screen.children())
        {
            this.gatherNavigationPointsFromListener(listener, navigate, cursorX, cursorY, points, null, null);
        }

        if(screen instanceof RecipeUpdateListener)
        {
            RecipeBookComponent recipeBook = ((RecipeUpdateListener) screen).getRecipeBookComponent();
            if(recipeBook.isVisible())
            {
                widgets.add(((RecipeBookComponentAccessor) recipeBook).controllableGetFilterButton());
                widgets.addAll(((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeTabs());

                RecipeBookPage page = ((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
                OverlayRecipeComponent overlay = ((RecipeBookPageAccessor) page).controllableGetOverlay();
                if(overlay.isVisible())
                {
                    widgets.addAll(((OverlayRecipeComponentAccessor) overlay).controllableGetRecipeButtons());
                }
                else
                {
                    RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
                    widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetForwardButton());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetBackButton());
                }
            }
        }

        // TODO should I look into abstracting this?

        if(screen instanceof EnchantmentScreen enchantmentScreen)
        {
            int startX = ClientServices.CLIENT.getScreenLeft(enchantmentScreen) + 60;
            int startY = ClientServices.CLIENT.getScreenTop(enchantmentScreen) + 14;
            int itemWidth = 108;
            int itemHeight = 19;
            for(int i = 0; i < 3; i++)
            {
                double itemX = startX + itemWidth / 2.0;
                double itemY = startY + itemHeight * i + itemHeight / 2.0;
                points.add(new BasicNavigationPoint(itemX, itemY));
            }
        }

        if(screen instanceof StonecutterScreen stonecutter)
        {
            StonecutterMenu menu = stonecutter.getMenu();
            int startX = ClientServices.CLIENT.getScreenLeft(stonecutter) + 52;
            int startY = ClientServices.CLIENT.getScreenTop(stonecutter) + 14;
            int buttonWidth = 16;
            int buttonHeight = 18;
            int offsetIndex = ClientServices.CLIENT.getStonecutterStartIndex(stonecutter);
            for(int index = offsetIndex; index < offsetIndex + 12 && index < menu.getNumRecipes(); index++)
            {
                int buttonIndex = index - offsetIndex;
                int buttonX = startX + buttonIndex % 4 * buttonWidth;
                int buttonY = startY + buttonIndex / 4 * buttonHeight + 2;
                points.add(new BasicNavigationPoint(buttonX + buttonWidth / 2.0, buttonY + buttonHeight / 2.0));
            }
        }

        for(AbstractWidget widget : widgets)
        {
            if(widget == null || widget.isHovered() || !widget.visible || !widget.active)
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

    private void gatherNavigationPointsFromListener(GuiEventListener listener, Navigate navigate, int cursorX, int cursorY, List<NavigationPoint> points, @Nullable AbstractSelectionList<?> list, @Nullable GuiEventListener entry)
    {
        if(listener instanceof Navigatable navigatable)
        {
            navigatable.elements().forEach(child ->
            {
                this.gatherNavigationPointsFromListener(child, navigate, cursorX, cursorY, points, list, entry);
            });
        }
        else if(listener instanceof AbstractSelectionList<?> selectionList)
        {
            this.gatherNavigationPointsFromAbstractList(selectionList, navigate, cursorX, cursorY, points);
        }
        else if(listener instanceof TabNavigationBar navigationBar)
        {
            navigationBar.children().forEach(child ->
            {
                if(child instanceof TabButton button)
                {
                    this.createWidgetNavigationPoint(button, points, list, entry);
                }
            });
        }
        else if(listener instanceof ContainerEventHandler handler)
        {
            handler.children().forEach(child ->
            {
                this.gatherNavigationPointsFromListener(child, navigate, cursorX, cursorY, points, list, entry);
            });
        }
        else if(listener instanceof AbstractWidget widget && widget.active && widget.visible)
        {
            this.createWidgetNavigationPoint(widget, points, list, entry);
        }
    }

    private void createWidgetNavigationPoint(AbstractWidget widget, List<NavigationPoint> points, @Nullable AbstractSelectionList<?> list, @Nullable GuiEventListener entry)
    {
        if(widget == null || widget.isHovered() || !widget.visible || !widget.active)
            return;
        int posX = widget.getX() + widget.getWidth() / 2;
        int posY = widget.getY() + widget.getHeight() / 2;
        if(list != null && entry != null)
        {
            points.add(new ListWidgetNavigationPoint(widget, list, entry));
        }
        else
        {
            points.add(new WidgetNavigationPoint(posX, posY, widget));
        }
    }

    private void gatherNavigationPointsFromAbstractList(AbstractSelectionList<?> list, Navigate navigate, int cursorX, int cursorY, List<NavigationPoint> points)
    {
        List<? extends GuiEventListener> children = list.children();
        int dir = navigate == Navigate.UP ? -1 : 1;
        int itemHeight = ClientServices.CLIENT.getListItemHeight(list);
        for(int i = 0; i < children.size(); i++)
        {
            GuiEventListener entry = children.get(i);
            int rowTop = ClientServices.CLIENT.getAbstractListRowTop(list, i);
            int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(list, i);
            int listTop = ClientServices.CLIENT.getAbstractListTop(list);
            int listBottom = ClientServices.CLIENT.getAbstractListBottom(list);
            if(rowTop > listTop - itemHeight && rowBottom < listBottom + itemHeight)
            {
                if(navigate == Navigate.UP || navigate == Navigate.DOWN)
                {
                    if(!(entry instanceof SkipItem) || (i != 0 && i != children.size() - 1))
                    {
                        points.add(new ListEntryNavigationPoint(list, entry, i, dir));
                    }
                }
                this.gatherNavigationPointsFromListener(entry, navigate, cursorX, cursorY, points, list, entry);
            }
            else if(list.isMouseOver(cursorX, cursorY))
            {
                points.add(new ListEntryNavigationPoint(list, entry, i, dir));
            }
        }
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

        RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) listener.getRecipeBookComponent()).controllableGetRecipeBookPage();
        RecipeButton recipeButton = ((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons().stream().filter(RecipeButton::isHoveredOrFocused).findFirst().orElse(null);
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

    private void moveCursorToClosestSlot(boolean moving, Screen screen)
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
            int cursorX = (int) (this.cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth());
            int cursorY = (int) (this.cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight());

            /* Finds the closest slot in the GUI within 14 pixels (inclusive) */
            Slot closestSlot = null;
            double closestDistance = -1.0;
            for(Slot slot : containerScreen.getMenu().slots)
            {
                int posX = guiLeft + slot.x + 8;
                int posY = guiTop + slot.y + 8;

                double distance = Math.sqrt(Math.pow(posX - cursorX, 2) + Math.pow(posY - cursorY, 2));
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
                double deltaX = slotCenterX - this.cursorX;
                double deltaY = slotCenterY - this.cursorY;

                if(!moving)
                {
                    if(cursorX != slotCenterXScaled || cursorY != slotCenterYScaled)
                    {
                        this.cursorX += deltaX * 0.75;
                        this.cursorY += deltaY * 0.75;
                    }
                    else
                    {
                        this.cursorSpeedX = 0.0F;
                        this.cursorSpeedY = 0.0F;
                    }
                }

                this.cursorSpeedX *= 0.75F;
                this.cursorSpeedY *= 0.75F;
            }
            else
            {
                this.cursorSpeedX = 0.0F;
                this.cursorSpeedY = 0.0F;
            }
        }
        else
        {
            this.cursorSpeedX = 0.0F;
            this.cursorSpeedY = 0.0F;
        }
    }

    private void setCursorPosition(double cursorX, double cursorY)
    {
        if(Config.CLIENT.client.options.virtualCursor.get())
        {
            this.virtualCursorX = cursorX;
            this.virtualCursorY = cursorY;
        }
        else
        {
            Minecraft mc = Minecraft.getInstance();
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), cursorX, cursorY);
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
            screen.mouseScrolled(this.getCursorX(), this.getCursorY(), Math.signum(dir));
            this.lastMerchantScroll = scrollTime;
        }
    }

    private double getCursorX()
    {
        Minecraft mc = Minecraft.getInstance();
        double cursorX = mc.mouseHandler.xpos();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && this.lastUse > 0)
        {
            cursorX = this.virtualCursorX;
        }
        return cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    private double getCursorY()
    {
        Minecraft mc = Minecraft.getInstance();
        double cursorY = mc.mouseHandler.ypos();
        if(Controllable.getController() != null && Config.CLIENT.client.options.virtualCursor.get() && this.lastUse > 0)
        {
            cursorY = this.virtualCursorY;
        }
        return cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
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
            double cursorX = this.getCursorX();
            double cursorY = this.getCursorY();
            this.invokeMouseClick(screen, button, cursorX, cursorY);
        }
    }

    private void invokeMouseClick(Screen screen, int button, double cursorX, double cursorY)
    {
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(button);
            ClientServices.CLIENT.setLastMouseEventTime(Blaze3D.getTime());
            ClientServices.CLIENT.sendScreenMouseClickPre(screen, cursorX, cursorY, button);
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
            double cursorX = this.getCursorX();
            double cursorY = this.getCursorY();
            this.invokeMouseReleased(screen, button, cursorX, cursorY);
        }
    }

    private void invokeMouseReleased(Screen screen, int button, double cursorX, double cursorY)
    {
        if(screen != null)
        {
            ClientServices.CLIENT.setActiveMouseButton(-1);
            ClientServices.CLIENT.sendScreenMouseReleasedPre(screen, cursorX, cursorY, button);
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

        public Comparator<NavigationPoint> getMinComparator(int cursorX, int cursorY)
        {
            return Comparator.comparing(p -> this.keyExtractor.apply(p, new Vector3d(cursorX, cursorY, 0)));
        }
    }

    private interface NavigatePredicate
    {
        boolean test(NavigationPoint point, int cursorX, int cursorY);
    }
}
