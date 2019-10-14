package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Mouse;

/**
 * Author: MrCrayfish
 */
public class Hooks
{
    public static void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && input.getLastUse() > 0)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mouseX = input.getVirtualMouseX() * screen.width / mc.displayWidth;
            mouseY = input.getVirtualMouseY() * screen.height / mc.displayHeight;
        }
        if(!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks)))
        {
            screen.drawScreen(mouseX, mouseY, partialTicks);
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
    }
}
