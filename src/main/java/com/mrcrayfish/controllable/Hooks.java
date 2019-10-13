package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Author: MrCrayfish
 */
public class Hooks
{
    public static void drawScreen(Screen screen, int mouseX, int mouseY, float partialTicks)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && input.getLastUse() > 0)
        {
            Minecraft minecraft = Minecraft.getInstance();
            mouseX = (int) (input.getVirtualMouseX() * (double) minecraft.mainWindow.getScaledWidth() / (double) minecraft.mainWindow.getWidth());
            mouseY = (int) (input.getVirtualMouseY() * (double) minecraft.mainWindow.getScaledHeight() / (double) minecraft.mainWindow.getHeight());
        }
        if(!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks)))
        {
            screen.render(mouseX, mouseY, partialTicks);
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
    }
}
