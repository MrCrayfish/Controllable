package com.mrcrayfish.controllable.client.binding;

import com.mrcrayfish.controllable.client.binding.handlers.EmptyHandler;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread-local flag that is {@code true} while a {@link KeyAdapterBinding} is in the process of
 * synthesizing and dispatching a keyboard event on behalf of a controller button press/release.
 * <p>
 * Other mods' {@code InputEvent.Key} subscribers can check this flag to avoid double-handling
 * actions that already have a dedicated Controllable integration.  Example:
 * <pre>
 *   if (KeyAdapterBinding.isControllerSynthesizedEvent()) return;
 * </pre>
 */

/**
 * A special binding that translates button presses to key presses. Supports multi-button.
 * Author: MrCrayfish
 */
public final class KeyAdapterBinding extends ButtonBinding
{
    /**
     * Set to {@code true} for the duration of {@link #handlePressed} so that keyboard-event
     * subscribers can detect that the event was synthesized by Controllable from a controller
     * input rather than from an actual physical key press.
     */
    private static final ThreadLocal<Boolean> CONTROLLER_SYNTHESIZED = ThreadLocal.withInitial(() -> false);

    /**
     * Returns {@code true} if the current {@code InputEvent.Key} (or {@code InputEvent.MouseButton})
     * being processed was synthesized by a {@link KeyAdapterBinding} on behalf of a controller
     * button.  Call this at the top of any {@code @SubscribeEvent} handler that should not fire
     * when a controller is driving the input.
     */
    public static boolean isControllerSynthesizedEvent()
    {
        return Boolean.TRUE.equals(CONTROLLER_SYNTHESIZED.get());
    }

    private final KeyMapping keyMapping;
    private final String labelKey;

    public KeyAdapterBinding(int button, KeyMapping mapping)
    {
        this(new HashSet<>(Collections.singleton(button)), mapping);
    }

    public KeyAdapterBinding(Set<Integer> buttons, KeyMapping mapping)
    {
        // You must call super with buttons and all required params. Adjust signature as in your ButtonBinding.
        super(buttons, mapping.getName() + ".custom", "key.categories.controllable_custom", ClientServices.CLIENT.createBindingContext(mapping), EmptyHandler.INSTANCE);
        this.keyMapping = mapping;
        this.labelKey = mapping.getName();
    }

    @Override
    public String getLabelKey()
    {
        return this.labelKey;
    }

    public KeyMapping getKeyMapping()
    {
        return this.keyMapping;
    }

    @Override
    protected void setPressed(boolean pressed)
    {
        boolean wasPressed = this.isButtonDown();
        super.setPressed(pressed);
        this.keyMapping.setDown(pressed);
        if(!wasPressed && pressed)
        {
            this.updateKeyBindPressTime();
            int key = ClientServices.CLIENT.getKeyValue(this.keyMapping);
            this.handlePressed(GLFW.GLFW_PRESS, key, 0);
        }
        else if(wasPressed && !pressed)
        {
            int key = ClientServices.CLIENT.getKeyValue(this.keyMapping);
            this.handlePressed(GLFW.GLFW_RELEASE, key, 0);
        }
    }

    /**
     * Overrides the base reset to also clear {@code keyMapping.setDown(false)} and fire a
     * synthetic GLFW release event. The base {@link ButtonBinding#resetPressedState()} only
     * clears the internal {@code pressed} field directly, bypassing {@link #setPressed} and
     * therefore leaving the underlying {@link net.minecraft.client.KeyMapping} stuck in a
     * held-down state — which causes mods that poll {@code keyMapping.isDown()} (e.g. Cobblemon
     * in its battle UI) to see the key as perpetually held after a screen transition.
     */
    @Override
    public void resetPressedState()
    {
        if(this.isButtonDown())
        {
            // Use setPressed(false) so keyMapping.setDown(false) and the GLFW release event
            // are both fired, cleanly unwinding any held state in downstream mods.
            this.setPressed(false);
        }
        else
        {
            // Not currently pressed — still ensure the KeyMapping is not stuck down.
            this.keyMapping.setDown(false);
            super.resetPressedState();
        }
    }

    private void updateKeyBindPressTime()
    {
        ClientServices.CLIENT.setKeyPressTime(this.keyMapping, 1);
    }

    private void handlePressed(int action, int key, int modifiers)
    {
        CONTROLLER_SYNTHESIZED.set(true);
        try
        {
            Screen screen = Minecraft.getInstance().screen;
            if(screen != null && ClientServices.CLIENT.sendScreenInput(screen, key, action, modifiers))
                return;
            ClientServices.CLIENT.sendKeyInputEvent(key, 0, action, modifiers);
        }
        finally
        {
            CONTROLLER_SYNTHESIZED.set(false);
        }
    }
}