package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Author: MrCrayfish
 */
public class Hooks
{
    /**
     * Used in order to fix block breaking progress. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean isLeftClicking()
    {
        Minecraft mc = Minecraft.getMinecraft();
        boolean isLeftClicking = mc.gameSettings.keyBindAttack.isKeyDown();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(ButtonBindings.ATTACK.isButtonDown())
            {
                isLeftClicking = true;
            }
        }
        boolean usingVirtualMouse = (Controllable.getOptions().isVirtualMouse() && Controllable.getInput().getLastUse() > 0);
        return mc.currentScreen == null && isLeftClicking && (Mouse.isGrabbed() || usingVirtualMouse);
    }

    /**
     * Used in order to fix actions like eating or pulling bow back. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean isRightClicking()
    {
        Minecraft mc = Minecraft.getMinecraft();
        boolean isRightClicking = mc.gameSettings.keyBindUseItem.isKeyDown();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(ButtonBindings.USE_ITEM.isButtonDown())
            {
                isRightClicking = true;
            }
        }
        return isRightClicking;
    }

    /**
     * Used in order to fix the quick move check in inventories. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean canQuickMove()
    {
        boolean isSneaking = (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(ButtonBindings.QUICK_MOVE.isButtonDown())
            {
                isSneaking = true;
            }
        }
        return isSneaking;
    }

    /**
     * Allows the player list to be shown. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean canShowPlayerList()
    {
        Minecraft mc = Minecraft.getMinecraft();
        boolean canShowPlayerList = mc.gameSettings.keyBindPlayerList.isKeyDown();
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(ButtonBindings.PLAYER_LIST.isButtonDown())
            {
                canShowPlayerList = true;
            }
        }
        return canShowPlayerList;
    }

    public static void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        ControllerInput input = Controllable.getInput();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && input.getLastUse() > 0)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mouseX = (int) (input.getVirtualMouseX() * screen.width / mc.displayWidth);
            mouseY = (int) (input.getVirtualMouseY() * screen.height / mc.displayHeight);
        }
        if(!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks)))
        {
            screen.drawScreen(mouseX, mouseY, partialTicks);
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
    }

    /**
     * Fixes selected item name rendering not being offset by console hotbar
     */
    @SuppressWarnings("unused")
    public static void applyHotbarOffset()
    {
        if(Controllable.getOptions().useConsoleHotbar())
        {
            GlStateManager.translate(0, -20, 0);
        }
    }
}
