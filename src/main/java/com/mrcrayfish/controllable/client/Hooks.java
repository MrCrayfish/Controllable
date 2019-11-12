package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
public class Hooks
{
    /**
     * Used in order to fix block breaking progress. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean isLeftClicking()
    {
        Minecraft mc = Minecraft.getInstance();
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
        return mc.currentScreen == null && isLeftClicking && (mc.mouseHelper.isMouseGrabbed() || usingVirtualMouse);
    }

    /**
     * Used in order to fix actions like eating or pulling bow back. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean isRightClicking()
    {
        Minecraft mc = Minecraft.getInstance();
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
        boolean canQuickMove = InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(ButtonBindings.QUICK_MOVE.isButtonDown())
            {
                canQuickMove = true;
            }
        }
        return canQuickMove;
    }

    /**
     * Allows the player list to be shown. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
    public static boolean canShowPlayerList()
    {
        Minecraft mc = Minecraft.getInstance();
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

    /**
     * Fixes the mouse position when virtual mouse is turned on for controllers. This method is linked via ASM.
     */
    @SuppressWarnings("unused")
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

    /**
     * Fixes selected item name rendering not being offset by console hotbar
     */
    @SuppressWarnings("unused")
    public static void applyHotbarOffset()
    {
        if(Controllable.getOptions().useConsoleHotbar())
        {
            GlStateManager.translated(0, -20, 0);
        }
    }
}
