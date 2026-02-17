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
 * A special binding that translates button presses to key presses. Supports multi-button.
 * Author: MrCrayfish
 */
public final class KeyAdapterBinding extends ButtonBinding
{
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

    private void updateKeyBindPressTime()
    {
        ClientServices.CLIENT.setKeyPressTime(this.keyMapping, 1);
    }

    private void handlePressed(int action, int key, int modifiers)
    {
        Screen screen = Minecraft.getInstance().screen;
        if(screen != null && ClientServices.CLIENT.sendScreenInput(screen, key, action, modifiers))
            return;
        ClientServices.CLIENT.sendKeyInputEvent(key, 0, action, modifiers);
    }
}