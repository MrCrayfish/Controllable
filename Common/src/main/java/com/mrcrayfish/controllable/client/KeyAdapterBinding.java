package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;

/**
 * A special binding that translates button presses to key presses. This binding does not need to be
 * registered and is added by players during runtime.
 *
 * Author: MrCrayfish
 */
public final class KeyAdapterBinding extends ButtonBinding
{
    private static Field pressedTimeField;
    private final KeyMapping keyMapping;
    private final String labelKey;

    public KeyAdapterBinding(int button, KeyMapping keyMapping)
    {
        //TODO finish this
        //keyMapping.getKeyConflictContext()
        super(button, keyMapping.getName() + ".custom", "key.categories.controllable_custom", null);
        this.keyMapping = keyMapping;
        this.labelKey = keyMapping.getName();
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
        super.setPressed(pressed);
        this.keyMapping.setDown(pressed);
        if(pressed) this.updateKeyBindPressTime();
        //TODO finish this
        //this.handlePressed(pressed ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE, this.keyMapping.getKey().getValue(), 0);
    }

    @Override
    protected void onPressTick()
    {
        //this.updateKeyBindPressTime();
    }

    private void updateKeyBindPressTime()
    {
        if(true) return;

        //TODO finish this

        if(pressedTimeField == null)
        {
            //pressedTimeField = ObfuscationReflectionHelper.findField(KeyMapping.class, "f_90818_");
        }
        try
        {
            pressedTimeField.set(this.keyMapping, 1);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void handlePressed(int action, int key, int modifiers)
    {
        Screen screen = Minecraft.getInstance().screen;
        if(screen != null)
        {
            if(ClientServices.CLIENT.sendScreenInput(screen, key, action, modifiers))
            {
                return;
            }
        }

        //TODO finish this
        //net.minecraftforge.client.ForgeHooksClient.onKeyInput(this.keyMapping.getKey().getValue(), 0, action, 0);
    }
}
