package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.screens.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.RelativePointerController;
import com.mrcrayfish.controllable.client.util.InputHelper;
import com.mrcrayfish.controllable.client.util.MouseHooks;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.controllable.mixin.client.TimerAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.framework.api.event.client.FrameworkClientTickEvents;
import com.mrcrayfish.framework.api.event.client.FrameworkScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Author: MrCrayfish
 */
public final class VirtualCursor
{
    private static volatile VirtualCursor instance;

    private final Vector2f inputVector = new Vector2f();
    private double prevX;
    private double prevY;
    private double x;
    private double y;
    private double renderX;
    private double renderY;
    private double lastMoveX;
    private double lastMoveY;
    private boolean visible;
    private boolean snapIfNoMove;
    private boolean initialized;
    private CursorMode mode = CursorMode.CONTROLLER;

    @ApiStatus.Internal
    public VirtualCursor()
    {
        Preconditions.checkState(instance == null, "Only one instance of VirtualCursor is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            FrameworkClientTickEvents.START_CLIENT.register(this::updateMovement);
            FrameworkClientTickEvents.START_RENDER.register(this::updateRenderPosition);
            FrameworkScreenEvents.OPENED.register(this::onScreenOpened);
            this.initialized = true;
        }
    }

    @ApiStatus.Internal
    public void resetToCenter()
    {
        Minecraft mc = Minecraft.getInstance();
        this.renderX = this.x = this.prevX = this.lastMoveX = mc.getWindow().getScreenWidth() / 2.0;
        this.renderY = this.y = this.prevY = this.lastMoveY = mc.getWindow().getScreenHeight() / 2.0;
    }

    public boolean isEnabled()
    {
        return !Config.CLIENT.options.disableVirtualCursor.get();
    }

    /**
     * @return True if the virtual cursor is visible
     */
    public boolean isVisible()
    {
        return this.mode.isController() && this.visible;
    }

    /**
     * Sets the visibility state of the virtual cursor
     *
     * @param visible true to make cursor visible in overlay
     */
    @ApiStatus.Internal
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * @return The x position the cursor
     */
    public int getX()
    {
        return (int) this.x;
    }

    /**
     * @return The y position the cursor
     */
    public int getY()
    {
        return (int) this.y;
    }

    /**
     * @return The x position of the cursor in screen space
     */
    public int getScreenX()
    {
        Minecraft mc = Minecraft.getInstance();
        return (int) (this.x * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth());
    }

    /**
     * @return The y position of the cursor in screen space
     */
    public int getScreenY()
    {
        Minecraft mc = Minecraft.getInstance();
        return (int) (this.y * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight());
    }

    /**
     * @return The x position of the cursor for rendering
     */
    public double getRenderX()
    {
        return this.renderX;
    }

    /**
     * @return The y position of the cursor for rendering
     */
    public double getRenderY()
    {
        return this.renderY;
    }

    /**
     * @return The x position of the cursor in screen space for rendering
     */
    public double getRenderScreenX()
    {
        Minecraft mc = Minecraft.getInstance();
        return this.renderX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    /**
     * @return The y position of the cursor in screen space for rendering
     */
    public double getRenderScreenY()
    {
        Minecraft mc = Minecraft.getInstance();
        return this.renderY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
    }

    /**
     * @return The current cursor mode
     */
    public CursorMode getMode()
    {
        return this.mode;
    }

    /**
     * Sets the cursor mode.
     *
     * @param mode the new cursor mode
     */
    @ApiStatus.Internal
    public void setMode(CursorMode mode)
    {
        this.mode = mode;
    }

    /**
     * Updates the movement of the cursor.
     */
    private void updateMovement()
    {
        // Don't do any updates if virtual cursor is disabled
        if(!this.isEnabled())
            return;

        this.prevX = this.x;
        this.prevY = this.y;
        this.inputVector.zero();

        // Don't update cursor if no controller is connected
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        // Don't update cursor if we're not in a screen, or we are in the layout screen
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null || mc.screen instanceof ControllerLayoutScreen)
            return;

        if(this.applyRelativePointerInput(controller, mc))
            return;

        this.updateInputVector(controller);

        // If the magnitude is greater than zero, input is being given
        if(this.inputVector.lengthSquared() > 0)
        {
            double cursorSpeed = Config.CLIENT.options.cursorSpeed.get() * Math.max(mc.getWindow().getGuiScale(), 1);

            // It's very easy to miss an interactable element, like widgets and container slots, when
            // moving at the full cursor speed. Instead, when hovering the element, the speed of the
            // cursor is slowed down to accommodate for the reaction time of the user.
            if(this.isHoveringContainerSlot() || this.isHoveringEventListener())
            {
                cursorSpeed *= Config.CLIENT.options.hoverModifier.get();
            }

            // Update cursor position based on movement vector and speed
            this.x += (int) (this.inputVector.x * cursorSpeed);
            this.y += (int) (this.inputVector.y * cursorSpeed);
            this.clampCursorToWindowBounds();

            // Make virtual cursor visible since it's being moved
            this.setVisible(true);

            // Update the last input time of this controller
            controller.updateInputTime();

            // Let the next update know to try snapping if no input detected
            this.snapIfNoMove = this.inputVector.lengthSquared() > 0; // Only snap if not gyro
            this.mode = CursorMode.CONTROLLER;
        }
        else if(this.snapIfNoMove)
        {
            this.snapToContainerSlot();
            this.snapIfNoMove = false;
        }

        // Send moved event to screens
        if(this.x != this.lastMoveX || this.y != this.lastMoveY)
        {
            MouseHooks.invokeMouseMoved(mc.screen, this.x, this.y, this.x - this.lastMoveX, this.y - this.lastMoveY);
            this.lastMoveX = this.x;
            this.lastMoveY = this.y;
        }
    }

    /**
     * Updates the rendering position of the cursor
     *
     * @param tracker the current delta tracker instance
     */
    private void updateRenderPosition(DeltaTracker tracker)
    {
        // Don't do any updates if virtual cursor is disabled
        if(!this.isEnabled())
            return;

        // Skip updating if no screen
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null)
            return;

        this.applyGyroInput(tracker);

        float partialTick = this.getPartialTick(tracker); // The normalised time between two ticks
        this.renderX = this.prevX + (this.x - this.prevX) * partialTick;
        this.renderY = this.prevY + (this.y - this.prevY) * partialTick;
    }

    /**
     * Gets the partial tick required for the virtual cursor. Unfortunately to get smooth movement
     * of the cursor when the game is paused, direct access to the delta tick is required since none
     * of the available methods return the correct value.
     *
     * @param tracker the delta tracker instance
     * @return a float containing the partial tick (0 to 1)
     */
    private float getPartialTick(DeltaTracker tracker)
    {
        if(tracker instanceof TimerAccessor accessor)
        {
            return accessor.controllable$DeltaTickResidual();
        }
        return tracker.getGameTimeDeltaPartialTick(true);
    }

    /**
     * Handles repositioning the cursor when a screen is opened. The cursor is moved to the center
     * of the window and the virtual cursor is marked as visible. If there was a previous screen,
     * updating is ignored.
     *
     * @param screen the screen that is about to be opened
     */
    private void onScreenOpened(Screen screen)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null)
            return;
        this.renderX = this.x = this.prevX = this.lastMoveX = mc.getWindow().getScreenWidth() / 2.0;
        this.renderY = this.y = this.prevY = this.lastMoveY = mc.getWindow().getScreenHeight() / 2.0;
        this.setVisible(true);
    }

    /**
     * Updates the input vector, which is read from the controller thumbsticks
     *
     * @param controller the controller instance to read the input from
     */
    private void updateInputVector(Controller controller)
    {
        float moveThreshold = 0.35F; // TODO change to config option
        float thumbstickX = this.getCursorInputX(controller);
        float thumbstickY = this.getCursorInputY(controller);
        float cursorVectorX = Math.abs(thumbstickX) >= moveThreshold ? thumbstickX : 0;
        float cursorVectorY = Math.abs(thumbstickY) >= moveThreshold ? thumbstickY : 0;
        this.inputVector.x = InputHelper.applyDeadzone(cursorVectorX, moveThreshold);
        this.inputVector.y = InputHelper.applyDeadzone(cursorVectorY, moveThreshold);
    }

    // steam controller trackpad deltas should move the cursor before thumbstick cursor input is used
    private boolean applyRelativePointerInput(Controller controller, Minecraft mc)
    {
        if(!(controller instanceof RelativePointerController relative))
            return false;

        Vector2f delta = relative.consumeRelativePointerDelta();
        if(delta.lengthSquared() <= 0)
            return false;

        double cursorSpeed = Math.max(mc.getWindow().getGuiScale(), 1) * Config.CLIENT.options.steamController.trackpadMenuSensitivity.get();
        this.x += delta.x * cursorSpeed;
        this.y += delta.y * cursorSpeed;
        this.clampCursorToWindowBounds();
        this.setVisible(true);
        this.snapIfNoMove = true;
        this.mode = CursorMode.CONTROLLER;
        controller.updateInputTime();

        if(this.x != this.lastMoveX || this.y != this.lastMoveY)
        {
            MouseHooks.invokeMouseMoved(mc.screen, this.x, this.y, this.x - this.lastMoveX, this.y - this.lastMoveY);
            this.lastMoveX = this.x;
            this.lastMoveY = this.y;
        }
        return true;
    }

    /**
     * Clamps the position of the cursor to the bounds of the window
     */
    private void clampCursorToWindowBounds()
    {
        Minecraft mc = Minecraft.getInstance();
        this.x = Math.max(0, Math.min(this.x, mc.getWindow().getWidth()));
        this.y = Math.max(0, Math.min(this.y, mc.getWindow().getHeight()));
    }

    /**
     * Gets the x input from the thumbstick assigned for controlling the cursor
     *
     * @param controller the controller instance to read the input from
     * @return a float ranging from -1 to 1
     */
    private float getCursorInputX(Controller controller)
    {
        float left = controller.getPressedValue(ButtonBindings.MOVE_CURSOR_LEFT.getButton());
        float right = controller.getPressedValue(ButtonBindings.MOVE_CURSOR_RIGHT.getButton());
        if(left > 0 && right > 0) // If both pressed, return no input
            return 0;
        return -left + right;
    }

    /**
     * Gets the y input from the thumbstick assigned for controlling the cursor
     *
     * @param controller the controller instance to read the input from
     * @return a float ranging from -1 to 1
     */
    private float getCursorInputY(Controller controller)
    {
        float up = controller.getPressedValue(ButtonBindings.MOVE_CURSOR_UP.getButton());
        float down = controller.getPressedValue(ButtonBindings.MOVE_CURSOR_DOWN.getButton());
        if(up > 0 && down > 0) // If both pressed, return no input
            return 0;
        return down - up;
    }

    /**
     * @return True if the cursor is hovering a container slot
     */
    private boolean isHoveringContainerSlot()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen instanceof AbstractContainerScreen<?> screen)
        {
            return ClientServices.CLIENT.getSlotUnderMouse(screen) != null;
        }
        return false;
    }

    /**
     * @return True if the cursor is hovering an event listener (e.g. a button)
     */
    private boolean isHoveringEventListener()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen == null)
            return false;
        // Convert the position to screen space before passing off
        double cursorScreenX = this.x * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double cursorScreenY = this.y * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        return ScreenHelper.findHoveredEventListenerExcludeList(mc.screen, cursorScreenX, cursorScreenY).isPresent();
    }

    /**
     * Moves the cursor to the given position. The position units must be in window space.
     *
     * @param x the x position of the cursor in window space
     * @param y the y position of the cursor in window space
     */
    public void jumpCursorTo(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.clampCursorToWindowBounds();
        this.renderX = this.prevX = this.lastMoveX = this.x;
        this.renderY = this.prevY = this.lastMoveY = this.y;
    }

    /**
     * Snaps the cursor to center of the currently hovered container slot, only if the slot has an
     * item or the cursor is currently carrying an item.
     */
    private void snapToContainerSlot()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.screen instanceof AbstractContainerScreen<?> screen)
        {
            Slot slot = ClientServices.CLIENT.getSlotUnderMouse(screen);
            if(slot != null && slot.isActive() && (slot.hasItem() || !screen.getMenu().getCarried().isEmpty()))
            {
                int slotX = ClientServices.CLIENT.getScreenLeft(screen) + slot.x + 8;
                int slotY = ClientServices.CLIENT.getScreenTop(screen) + slot.y + 8;
                slotX *= (int) mc.getWindow().getGuiScale();
                slotY *= (int) mc.getWindow().getGuiScale();
                this.x = slotX;
                this.y = slotY;
                this.clampCursorToWindowBounds();
            }
        }
    }

    /**
     * Applies gyro input to move the cursor. Only applies if enabled by the player.
     *
     * @param tracker a DeltaTracker instance
     */
    private void applyGyroInput(DeltaTracker tracker)
    {
        Controller controller = Controllable.getController();
        if(controller == null || !controller.supportsGyroscope() || !Config.CLIENT.options.gyro.gyroMouse.get())
            return;

        Vector3f gyroscope = controller.getGyroscope();
        float gyroX = -gyroscope.y;
        float gyroY = -gyroscope.x;

        // Apply a threshold to prevent movement jitters
        float jitterThreshold = 0.025F;
        if(!(Math.abs(gyroX) >= jitterThreshold) && !(Math.abs(gyroY) >= jitterThreshold))
            return;

        // Input detected, so update last input time
        controller.updateInputTime();

        // Invert y-input if enabled
        if(Config.CLIENT.options.gyro.invertY.get())
            gyroY = -gyroY;

        // Scale the gyro input by the configured gyro speed
        float gyroSpeed = Config.CLIENT.options.gyro.gyroSpeed.get().floatValue();
        float partialTick = tracker.getGameTimeDeltaTicks();
        gyroX *= (gyroSpeed * partialTick) * Config.CLIENT.options.gyro.horizontalSensitivity.get().floatValue();
        gyroY *= (gyroSpeed * partialTick) * Config.CLIENT.options.gyro.verticalSensitivity.get().floatValue();

        double beforeX = this.x;
        double beforeY = this.y;

        // Add the gyro input to the x and y positions
        this.x += gyroX;
        this.y += gyroY;

        // Ensure it doesn't go out of the window bounds
        this.clampCursorToWindowBounds();

        // Also add the gyro input to the previous positions but only the delta
        this.prevX += (this.x - beforeX);
        this.prevY += (this.y - beforeY);

        // Ensure the cursor is visible if positions changed
        this.setVisible(true);
        this.mode = CursorMode.CONTROLLER;
    }

    public enum CursorMode
    {
        CONTROLLER, MOUSE;

        public boolean isController()
        {
            return this == CONTROLLER;
        }

        public boolean isMouse()
        {
            return this == MOUSE;
        }
    }
}
