package com.mrcrayfish.controllable.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class PressableButton extends GuiButton
{
    private Consumer<GuiButton> onPress;

    public PressableButton(int x, int y, String buttonText, Consumer<GuiButton> onPress)
    {
        super(-1, x, y, buttonText);
        this.onPress = onPress;
    }

    public PressableButton(int x, int y, int widthIn, int heightIn, String buttonText, Consumer<GuiButton> onPress)
    {
        super(-1, x, y, widthIn, heightIn, buttonText);
        this.onPress = onPress;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if(this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height)
        {
            this.onPress.accept(this);
            return true;
        }
        return false;
    }
}
