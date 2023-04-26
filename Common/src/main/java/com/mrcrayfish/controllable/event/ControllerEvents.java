package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.FrameworkEvent;
import com.mrcrayfish.framework.api.event.IFrameworkEvent;

import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ControllerEvents
{
    public static final FrameworkEvent<Input> INPUT = new FrameworkEvent<>(listeners -> (controller, newButton, originalButton, state) -> {
        for(var listener : listeners) {
            if(listener.handle(controller, newButton, originalButton, state)) {
                return true;
            }
        }
        return false;
    });

    public static final FrameworkEvent<Button> BUTTON = new FrameworkEvent<>(listeners -> (controller) -> {
        for(var listener : listeners) {
            if(listener.handle(controller)) {
                return true;
            }
        }
        return false;
    });

    public static final FrameworkEvent<UpdateMovement> UPDATE_MOVEMENT = new FrameworkEvent<>(listeners -> () -> {
        for(var listener : listeners) {
            if(listener.handle()) {
                return true;
            }
        }
        return false;
    });

    public static final FrameworkEvent<UpdateCamera> UPDATE_CAMERA = new FrameworkEvent<>(listeners -> (yawSpeed, pitchSpeed) -> {
        for(var listener : listeners) {
            if(listener.handle(yawSpeed, pitchSpeed)) {
                return true;
            }
        }
        return false;
    });

    public static final FrameworkEvent<GatherActions> GATHER_ACTIONS = new FrameworkEvent<>(listeners -> (actions, visibility) -> {
        listeners.forEach(listener -> listener.handle(actions, visibility));
    });

    public static final FrameworkEvent<GatherNavigationPoints> GATHER_NAVIGATION_POINTS = new FrameworkEvent<>(listeners -> (points) -> {
        listeners.forEach(listener -> listener.handle(points));
    });

    public static final FrameworkEvent<GatherRadialMenuItems> GATHER_RADIAL_MENU_ITEMS = new FrameworkEvent<>(listeners -> (items) -> {
        listeners.forEach(listener -> listener.handle(items));
    });

    public static final FrameworkEvent<RenderHints> RENDER_HINTS = new FrameworkEvent<>(listeners -> () -> {
        for(var listener : listeners) {
            if(listener.handle()) {
                return true;
            }
        }
        return false;
    });

    public static final FrameworkEvent<RenderMiniPlayer> RENDER_MINI_PLAYER = new FrameworkEvent<>(listeners -> () -> {
        for(var listener : listeners) {
            if(listener.handle()) {
                return true;
            }
        }
        return false;
    });

    @FunctionalInterface
    public interface Input extends IFrameworkEvent
    {
        boolean handle(Controller controller, Value<Integer> newButton, int originalButton, boolean state);
    }

    @FunctionalInterface
    public interface Button extends IFrameworkEvent
    {
        boolean handle(Controller controller);
    }

    @FunctionalInterface
    public interface UpdateMovement extends IFrameworkEvent
    {
        boolean handle();
    }

    @FunctionalInterface
    public interface UpdateCamera extends IFrameworkEvent
    {
        boolean handle(Value<Float> yawSpeed, Value<Float> pitchSpeed);
    }

    @FunctionalInterface
    public interface GatherActions extends IFrameworkEvent
    {
        void handle(Map<ButtonBinding, Action> actions, ActionVisibility visibility);
    }

    @FunctionalInterface
    public interface GatherNavigationPoints extends IFrameworkEvent
    {
        void handle(List<NavigationPoint> points);
    }

    @FunctionalInterface
    public interface GatherRadialMenuItems extends IFrameworkEvent
    {
        void handle(List<RadialMenuHandler.AbstractRadialItem> items);
    }

    @FunctionalInterface
    public interface RenderHints extends IFrameworkEvent
    {
        boolean handle();
    }

    @FunctionalInterface
    public interface RenderMiniPlayer extends IFrameworkEvent
    {
        boolean handle();
    }
}
