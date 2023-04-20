package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.util.ReflectUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * A special binding that translates button presses to key presses. This binding does not need to be
 * registered and is added by players during runtime.
 *
 * Author: MrCrayfish
 */
public final class KeyAdapterBinding extends ButtonBinding
{
    private final KeyMapping keyMapping;
    private final String labelKey;

    public KeyAdapterBinding(int button, KeyMapping mapping)
    {
        super(button, mapping.getName() + ".custom", "key.categories.controllable_custom", new ForgeCompatBindingContext(mapping.getKeyConflictContext()));
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
        super.setPressed(pressed);
        this.keyMapping.setDown(pressed);
        if(pressed) this.updateKeyBindPressTime();
        int key = this.keyMapping.getKey().getValue();
        this.handlePressed(pressed ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE, key, 0);
    }

    @Override
    protected void onPressTick()
    {
        //this.updateKeyBindPressTime();
    }

    private void updateKeyBindPressTime()
    {
        ReflectUtil.setKeyPressTime(this.keyMapping, 1);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void handlePressed(int action, int key, int modifiers)
    {
        Screen screen = Minecraft.getInstance().screen;
        if(screen != null)
        {
            boolean[] cancelled = new boolean[]{false};
            Screen.wrapScreenError(() ->
            {
                if(action == GLFW.GLFW_RELEASE)
                {
                    cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyReleasedPre(screen, key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = screen.keyReleased(key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyReleasedPost(screen, key, -1, modifiers);
                }
                else if(action == GLFW.GLFW_PRESS)
                {
                    cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyPressedPre(screen, key, -1, modifiers);
                    if(!cancelled[0])  cancelled[0] = screen.keyPressed(key, -1, modifiers);
                    if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyPressedPost(screen, key, -1, modifiers);
                }

            }, "keyPressed event handler", screen.getClass().getCanonicalName());
            if(cancelled[0])
            {
                return;
            }
        }
        net.minecraftforge.client.ForgeHooksClient.onKeyInput(this.keyMapping.getKey().getValue(), 0, action, 0);
    }
}
