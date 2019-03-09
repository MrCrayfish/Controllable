package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Buttons;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.event.ControllerInputEvent;
import com.mrcrayfish.controllable.event.ControllerMoveEvent;
import com.mrcrayfish.controllable.event.ControllerTurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class Events
{
    private boolean keyboardSneaking = false;
    private boolean sneaking = false;

    private float prevXAxis;
    private float prevYAxis;
    private int prevTargetMouseX;
    private int prevTargetMouseY;
    private int targetMouseX;
    private int targetMouseY;

    private int pressedDpadX = -1;
    private int pressedDpadY = -1;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            this.prevTargetMouseX = targetMouseX;
            this.prevTargetMouseY = targetMouseY;

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            Minecraft mc = Minecraft.getMinecraft();
            if(mc.inGameHasFocus)
                return;

            if(controller.getXAxisValue() != 0.0F || controller.getYAxisValue() != 0.0F)
            {
                if(prevXAxis == 0.0F && prevYAxis == 0.0F)
                {
                    prevTargetMouseX = targetMouseX = Mouse.getX();
                    prevTargetMouseY = targetMouseY = Mouse.getY();
                }
                targetMouseX += 20 * (controller.getXAxisValue() > 0.0F ? 1 : -1) * Math.abs(controller.getXAxisValue());
                targetMouseY += 20 * (controller.getYAxisValue() > 0.0F ? -1 : 1) * Math.abs(controller.getYAxisValue());
            }

            prevXAxis = controller.getXAxisValue();
            prevYAxis = controller.getYAxisValue();
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        while(Controllers.next())
        {
            if(Controllers.isEventButton() && Controllers.getEventSource() == controller)
            {
                int button = Controllers.getEventControlIndex();
                boolean state = Controllers.getEventButtonState();
                if(!MinecraftForge.EVENT_BUS.post(new ControllerInputEvent(button, state)))
                {
                    handleMinecraftInput(button, state);
                }
            }
        }

        this.handleDpadInput(controller);

        if(event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if(player == null)
            return;

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerTurnEvent()))
            {
                /* Handles rotating the yaw of player */
                if(controller.getZAxisValue() != 0.0F || controller.getRZAxisValue() != 0.0F)
                {
                    float rotationYaw = 20.0F * (controller.getZAxisValue() > 0.0F ? 1 : -1) * Math.abs(controller.getZAxisValue());
                    float rotationPitch = 15.0F * (controller.getRZAxisValue() > 0.0F ? 1 : -1) * Math.abs(controller.getRZAxisValue());
                    player.turn(rotationYaw, -rotationPitch);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderScreen(RenderWorldLastEvent event)
    {
        if(Minecraft.getMinecraft().currentScreen != null && (targetMouseX != prevTargetMouseX || targetMouseY != prevTargetMouseY))
        {
            int mouseX = (int) (prevTargetMouseX + (targetMouseX - prevTargetMouseX) * event.getPartialTicks() + 0.5F);
            int mouseY = (int) (prevTargetMouseY + (targetMouseY - prevTargetMouseY) * event.getPartialTicks() + 0.5F);
            Mouse.setCursorPosition(mouseX, mouseY);
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getMinecraft();

        if(keyboardSneaking && !mc.gameSettings.keyBindSneak.isKeyDown())
        {
            sneaking = false;
            keyboardSneaking = false;
        }

        if(mc.gameSettings.keyBindSneak.isKeyDown())
        {
            sneaking = true;
            keyboardSneaking = true;
        }

        event.getMovementInput().sneak = sneaking;

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerMoveEvent()))
            {
                if(controller.getYAxisValue() != 0.0F)
                {
                    int dir = controller.getYAxisValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * Math.abs(controller.getYAxisValue());

                    if(event.getMovementInput().sneak)
                    {
                        event.getMovementInput().moveForward *= 0.3D;
                    }
                }

                if(controller.getXAxisValue() != 0.0F)
                {
                    int dir = controller.getXAxisValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * Math.abs(controller.getXAxisValue());

                    if(event.getMovementInput().sneak)
                    {
                        event.getMovementInput().moveStrafe *= 0.3D;
                    }
                }
            }

            if(controller.isButtonPressed(Buttons.A))
            {
                event.getMovementInput().jump = true;
            }
        }

        if(controller.isButtonPressed(Buttons.LEFT_TRIGGER) && mc.rightClickDelayTimer == 0 && !mc.player.isHandActive())
        {
            mc.rightClickMouse();
        }
    }

    private void handleDpadInput(Controller controller)
    {
        float povX = controller.getPovX();
        if(povX != 0.0F)
        {
            if(pressedDpadX == -1)
            {
                pressedDpadX = povX > 0.0F ? Buttons.DPAD_RIGHT : Buttons.DPAD_LEFT;
                handleMinecraftInput(pressedDpadX, true);
            }
        }
        else if(pressedDpadX != -1)
        {
            handleMinecraftInput(pressedDpadX, false);
            pressedDpadX = -1;
        }

        float povY = controller.getPovY();
        if(povY != 0.0F)
        {
            if(pressedDpadY == -1)
            {
                pressedDpadY = povY > 0.0F ? Buttons.DPAD_DOWN : Buttons.DPAD_UP;
                handleMinecraftInput(pressedDpadY, true);
            }
        }
        else if(pressedDpadY != -1)
        {
            handleMinecraftInput(pressedDpadY, false);
            pressedDpadY = -1;
        }
    }

    private void handleMinecraftInput(int button, boolean state)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(state)
        {
            if(button == Buttons.Y)
            {
                if(mc.currentScreen == null)
                {
                    if (mc.playerController.isRidingHorse())
                    {
                        mc.player.sendHorseInventory();
                    }
                    else
                    {
                        mc.getTutorial().openInventory();
                        mc.displayGuiScreen(new GuiInventory(mc.player));
                    }
                    prevTargetMouseX = targetMouseX = Mouse.getX();
                    prevTargetMouseY = targetMouseY = Mouse.getY();
                }
                else
                {
                    mc.player.closeScreen();
                }
            }
            else if(button == Buttons.LEFT_THUMB_STICK)
            {
                if(mc.currentScreen == null)
                {
                    sneaking = !sneaking;
                }
            }
            else if(button == Buttons.LEFT_BUMPER)
            {
                if(mc.currentScreen == null)
                {
                    mc.player.inventory.changeCurrentItem(1);
                }
            }
            else if(button == Buttons.RIGHT_BUMPER)
            {
                if(mc.currentScreen == null)
                {
                    mc.player.inventory.changeCurrentItem(-1);
                }
            }
            else if(button == Buttons.A && mc.currentScreen != null)
            {
                invokeMouseClick(mc.currentScreen, 0);
            }
            else if(button == Buttons.X && mc.currentScreen != null)
            {
                invokeMouseClick(mc.currentScreen, 1);
            }
            else if(button == Buttons.DPAD_UP)
            {
                cycleThirdPersonView();
            }
            else
            {
                Controller controller = Controllable.getController();
                if(controller == null)
                    return;

                if(!mc.player.isHandActive() && mc.currentScreen == null)
                {
                    if(button == Buttons.RIGHT_TRIGGER)
                    {
                        mc.clickMouse();
                    }
                    if(button == Buttons.LEFT_TRIGGER)
                    {
                        mc.rightClickMouse();
                    }
                    if(button == Buttons.X)
                    {
                        mc.middleClickMouse();
                    }
                }
            }
        }
        else if(button == Buttons.A && mc.currentScreen != null)
        {
            invokeMouseReleased(mc.currentScreen, 0);
        }
        else if(button == Buttons.X && mc.currentScreen != null)
        {
            invokeMouseReleased(mc.currentScreen, 1);
        }
    }

    /**
     * Cycles the third person view. Minecraft doesn't have this code in a convenient method.
     */
    private void cycleThirdPersonView()
    {
        Minecraft mc = Minecraft.getMinecraft();

        mc.gameSettings.thirdPersonView++;
        if(mc.gameSettings.thirdPersonView > 2)
        {
            mc.gameSettings.thirdPersonView = 0;
        }

        if(mc.gameSettings.thirdPersonView == 0)
        {
            mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
        }
        else if(mc.gameSettings.thirdPersonView == 1)
        {
            mc.entityRenderer.loadEntityShader(null);
        }
    }

    /**
     * Invokes a mouse click in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param gui the gui instance
     * @param button the button to click with
     */
    private void invokeMouseClick(GuiScreen gui, int button)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(gui != null)
        {
            int guiX = Mouse.getX() * gui.width / mc.displayWidth;
            int guiY = gui.height - Mouse.getY() * gui.height / mc.displayHeight - 1;

            try
            {
                Class<?> clazz = GuiScreen.class;
                Field eventButton = clazz.getDeclaredField("eventButton");
                eventButton.setAccessible(true);
                eventButton.set(gui, button);

                Field lastMouseEvent = clazz.getDeclaredField("lastMouseEvent");
                lastMouseEvent.setAccessible(true);
                lastMouseEvent.set(gui, System.currentTimeMillis());

                Method mouseClicked = clazz.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                mouseClicked.setAccessible(true);
                mouseClicked.invoke(gui, guiX, guiY, button);
            }
            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Invokes a mouse released in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param gui the gui instance
     * @param button the button to click with
     */
    private void invokeMouseReleased(GuiScreen gui, int button)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(gui != null)
        {
            int guiX = Mouse.getX() * gui.width / mc.displayWidth;
            int guiY = gui.height - Mouse.getY() * gui.height / mc.displayHeight - 1;

            try
            {
                Class<?> clazz = GuiScreen.class;
                Field eventButton = clazz.getDeclaredField("eventButton");
                eventButton.setAccessible(true);
                eventButton.set(gui, -1);

                //Resets the mouse straight away
                Method mouseReleased = clazz.getDeclaredMethod("mouseReleased", int.class, int.class, int.class);
                mouseReleased.setAccessible(true);
                mouseReleased.invoke(gui, guiX, guiY, button);
            }
            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used in order to fix block breaking progress. This method is linked via ASM.
     */
    public static boolean isLeftClicking()
    {
        Minecraft mc = Minecraft.getMinecraft();
        boolean isLeftClicking = mc.gameSettings.keyBindAttack.isKeyDown();
        Controller controller = Controllable.getController();
        if(controller != null) isLeftClicking |= controller.isButtonPressed(Buttons.RIGHT_TRIGGER);
        return mc.currentScreen == null && isLeftClicking && mc.inGameHasFocus;
    }

    /**
     * Used in order to fix actions like eating or pulling bow back. This method is linked via ASM.
     */
    public static boolean isRightClicking()
    {
        Minecraft mc = Minecraft.getMinecraft();
        boolean isRightClicking = mc.gameSettings.keyBindUseItem.isKeyDown();
        Controller controller = Controllable.getController();
        if(controller != null) isRightClicking |= controller.isButtonPressed(Buttons.LEFT_TRIGGER);
        return isRightClicking;
    }
}
