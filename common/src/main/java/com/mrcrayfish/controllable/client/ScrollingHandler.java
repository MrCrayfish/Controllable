package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.InputHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import org.jetbrains.annotations.ApiStatus;

/**
 * Author: MrCrayfish
 */
public class ScrollingHandler
{
    private static ScrollingHandler instance;
    private static final float ABSTRACT_LIST_SCROLL_THRESHOLD = 0.2F;
    private static final float SCROLL_THRESHOLD = ABSTRACT_LIST_SCROLL_THRESHOLD + 0.2F; // Needs to be greater for logic to work

    private long lastScrollTime;
    private boolean initialized;

    @ApiStatus.Internal
    public ScrollingHandler()
    {
        Preconditions.checkState(instance == null, "Only one instance of ScrollingHandler is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            TickEvents.START_RENDER.register(this::scrollScreensWithLists);
            ScreenEvents.OPENED.register(this::onScreenOpened);
            this.initialized = true;
        }
    }

    /**
     * Reset the last scroll time to ensure scrolls can happen straight away
     *
     * @param screen the screen that is being opened
     */
    private void onScreenOpened(Screen screen)
    {
        this.lastScrollTime = 0;
    }

    /**
     * Handles scrolling input to screens
     *
     * @param tracker a delta tracker instance for rendering
     */
    private void scrollScreensWithLists(DeltaTracker tracker)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null)
        {
            // Apply custom scrolling for abstract list widgets
            if(this.handleAbstractListScrolling(mc.screen, controller))
                return;

            // Send generic scroll events to screen
            this.handleScreenScrolling(mc.screen, controller);
        }
    }

    /**
     * Handles scrolling the list of available trades in the merchant screen. Merchant screens can
     * be found in the game, notably used by villagers - which they scam your emeralds for items.
     *
     * @param screen the merchant screen instance
     * @param controller the controller providing the input for scrolling
     */
    private void handleScreenScrolling(Screen screen, Controller controller)
    {
        float input = this.getScrollingInputY(controller);
        if(Math.abs(input) >= SCROLL_THRESHOLD)
        {
            int screenCursorX = Controllable.getCursor().getScreenX();
            int screenCursorY = Controllable.getCursor().getScreenY();
            if(this.usePreciseInput(screen))
            {
                input = InputHelper.applyDeadzone(input, SCROLL_THRESHOLD);
                input = this.scaleInputForScreen(screen, input);
                input *= Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                screen.mouseScrolled(screenCursorX, screenCursorY, 0, -input);
            }
            else
            {
                long scrollTime = Util.getMillis();
                if(scrollTime - this.lastScrollTime >= this.getScrollIntervalInMillis(screen))
                {
                    input = Math.signum(input);
                    input = this.scaleInputForScreen(screen, input);
                    screen.mouseScrolled(screenCursorX, screenCursorY, 0, -input);
                    this.lastScrollTime = scrollTime;
                }
            }
            controller.updateInputTime();
        }
        else
        {
            // Do this to allow thumbstick to be tapped up or down
            this.lastScrollTime = 0;
        }
    }

    /**
     * Gets the time interval to wait between scrolling. Since scrolling is handled during the
     * rendering of a frame, to not scroll too fast, scrolling is based on time intervals.
     *
     * @param screen the screen to get the specific interval
     * @return a long value of the time interval in millis
     */
    private long getScrollIntervalInMillis(Screen screen)
    {
        return switch(screen) {
            case MerchantScreen s -> 150;
            case LoomScreen s -> 100;
            case StonecutterScreen s -> 120;
            case CreativeModeInventoryScreen s -> 65;
            default -> 50;
        };
    }

    /**
     * Some screens allow the scrolling value to be any number between negative one and one. This
     * method returns true for screens that allow more precise input for scrolling.
     *
     * @param screen the screen to test
     * @return True if you can use precise input for scrolling
     */
    private boolean usePreciseInput(Screen screen)
    {
        return switch(screen) {
            case AdvancementsScreen s -> true;
            default -> false;
        };
    }

    /**
     * Some screens allow the scrolling value to be any number between negative one and one. This
     * method returns true for screens that allow more precise input for scrolling.
     *
     * @param screen the screen to test
     * @return True if you can use precise input for scrolling
     */
    private float scaleInputForScreen(Screen screen, float input)
    {
        return switch(screen) {
            case ChatScreen s -> Math.signum(input) * 0.15F;
            default -> input;
        };
    }

    /**
     * Handles scrolling abstract lists. Abstract lists are considered widgets and can be found on
     * the world selection screen (the menu displaying all your worlds from the saves folder).
     *
     * @param screen the merchant screen instance
     * @param controller the controller providing the input for scrolling
     */
    private boolean handleAbstractListScrolling(Screen screen, Controller controller)
    {
        float input = this.getScrollingInputY(controller);
        if(Math.abs(input) >= ABSTRACT_LIST_SCROLL_THRESHOLD)
        {
            double cursorX = Controllable.getCursor().getRenderScreenX();
            double cursorY = Controllable.getCursor().getRenderScreenY();
            GuiEventListener hoveredListener = ScreenHelper.findHoveredListener(screen, cursorX, cursorY, listener -> listener instanceof AbstractSelectionList<?>).orElse(null);
            if(hoveredListener instanceof AbstractSelectionList<?> list)
            {
                double direction = InputHelper.applyDeadzone(input, ABSTRACT_LIST_SCROLL_THRESHOLD);
                direction *= Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                list.setScrollAmount(list.getScrollAmount() + direction * Config.CLIENT.options.listScrollSpeed.get());
                controller.updateInputTime();
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the y input from the thumbstick assigned for scrolling. This is usually the opposite
     * thumbstick of the cursor.
     *
     * @param controller the controller instance to read the input from
     * @return a float ranging from -1 to 1
     */
    private float getScrollingInputY(Controller controller)
    {
        float up = controller.getPressedValue(ButtonBindings.SCROLL_UP.getButton());
        float down = controller.getPressedValue(ButtonBindings.SCROLL_DOWN.getButton());
        if(up > 0 && down > 0) // If both pressed, return no input
            return 0;
        return -up + down;
    }
}
