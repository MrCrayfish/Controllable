package com.mrcrayfish.controllable.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A special binding that translates button presses to key presses. This binding does not need to be
 * registered and is added by players during runtime.
 *
 * Author: MrCrayfish
 */
public final class KeyAdapterBinding extends ButtonBinding
{
    private static Field pressedTimeField;
    private final KeyBinding keyBinding;
    private final String labelKey;

    public KeyAdapterBinding(int button, KeyBinding keyBinding)
    {
        super(button, keyBinding.getKeyDescription() + ".custom", "key.categories.controllable_custom", keyBinding.getKeyConflictContext());
        this.keyBinding = keyBinding;
        this.labelKey = keyBinding.getKeyDescription();
    }

    @Override
    public String getLabelKey()
    {
        return this.labelKey;
    }

    public KeyBinding getKeyBinding()
    {
        return this.keyBinding;
    }

    @Override
    protected void setPressed(boolean pressed)
    {
        super.setPressed(pressed);
        this.keyBinding.setPressed(pressed);
        if(pressed) this.updateKeyBindPressTime();
        this.handlePressed(pressed ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE, this.keyBinding.getKey().getKeyCode(), 0);
    }

    @Override
    protected void onPressTick()
    {
        //this.updateKeyBindPressTime();
    }

    private void updateKeyBindPressTime()
    {
        if(pressedTimeField == null)
        {
            pressedTimeField = ObfuscationReflectionHelper.findField(KeyBinding.class, "field_151474_i");
        }
        try
        {
            pressedTimeField.set(this.keyBinding, 1);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void handlePressed(int action, int key, int modifiers)
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen != null)
        {
            boolean[] cancelled = new boolean[]{false};
            Screen.wrapScreenError(() ->
            {
                if(action == GLFW.GLFW_RELEASE)
                {
                    cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyReleasedPre(screen, key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = screen.keyReleased(key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyReleasedPost(screen, key, -1, modifiers);
                }
                else if(action == GLFW.GLFW_PRESS)
                {
                    cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyPressedPre(screen, key, -1, modifiers);
                    if(!cancelled[0])  cancelled[0] = screen.keyPressed(key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyPressedPost(screen, key, -1, modifiers);
                }

            }, "keyPressed event handler", screen.getClass().getCanonicalName());
            if(cancelled[0])
            {
                return;
            }
        }
        net.minecraftforge.client.ForgeHooksClient.fireKeyInput(this.keyBinding.getKey().getKeyCode(), 0, action, 0);
    }
}
