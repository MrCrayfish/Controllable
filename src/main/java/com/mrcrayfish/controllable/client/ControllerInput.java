package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.GuiControllerLayout;
import com.mrcrayfish.controllable.event.ControllerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class ControllerInput
{
    public static int lastUse = 0;

    private boolean keyboardSneaking = false;
    private boolean sneaking = false;
    private boolean isFlying = false;

    private float prevXAxis;
    private float prevYAxis;
    private int prevTargetMouseX;
    private int prevTargetMouseY;
    private int targetMouseX;
    private int targetMouseY;
    private float mouseSpeedX;
    private float mouseSpeedY;

    private int dropCounter = -1;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            prevTargetMouseX = targetMouseX;
            prevTargetMouseY = targetMouseY;

            if(lastUse > 0)
            {
                lastUse--;
            }

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            Minecraft mc = Minecraft.getMinecraft();
            if(mc.inGameHasFocus)
                return;

            if(mc.currentScreen == null || mc.currentScreen instanceof GuiControllerLayout)
                return;

            /* Only need to run code if left thumb stick has input */
            boolean moving = controller.getLThumbStickXValue() != 0.0F || controller.getLThumbStickYValue() != 0.0F;
            if(moving)
            {
                lastUse = 100;

                /* Updates the target mouse position when the initial thumb stick movement is
                 * detected. This fixes an issue when the user moves the cursor with the mouse then
                 * switching back to controller, the cursor would jump to old target mouse position. */
                if(prevXAxis == 0.0F && prevYAxis == 0.0F)
                {
                    prevTargetMouseX = targetMouseX = Mouse.getX();
                    prevTargetMouseY = targetMouseY = Mouse.getY();
                }

                float xAxis = (controller.getLThumbStickXValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickXValue());
                if(Math.abs(xAxis) > 0.35F)
                {
                    mouseSpeedX = xAxis;
                }
                else
                {
                    mouseSpeedX = 0.0F;
                }

                float yAxis = (controller.getLThumbStickYValue() > 0.0F ? 1 : -1) * Math.abs(controller.getLThumbStickYValue());
                if(Math.abs(yAxis) > 0.35F)
                {
                    mouseSpeedY = yAxis;
                }
                else
                {
                    mouseSpeedY = 0.0F;
                }
            }

            if(Math.abs(mouseSpeedX) > 0.05F || Math.abs(mouseSpeedY) > 0.05F)
            {
                targetMouseX += 30 * mouseSpeedX;
                targetMouseY += 30 * mouseSpeedY;
            }

            prevXAxis = controller.getLThumbStickXValue();
            prevYAxis = controller.getLThumbStickYValue();

            this.moveMouseToClosestSlot(moving, mc.currentScreen);

            if(mc.currentScreen instanceof GuiContainerCreative)
            {
                this.handleCreativeScrolling((GuiContainerCreative) mc.currentScreen, controller);
            }
        }
    }

    @SubscribeEvent
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        if(Minecraft.getMinecraft().currentScreen != null && (targetMouseX != prevTargetMouseX || targetMouseY != prevTargetMouseY))
        {
            if(!(Minecraft.getMinecraft().currentScreen instanceof GuiControllerLayout))
            {
                float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
                int mouseX = (int) (prevTargetMouseX + (targetMouseX - prevTargetMouseX) * partialTicks + 0.5F);
                int mouseY = (int) (prevTargetMouseY + (targetMouseY - prevTargetMouseY) * partialTicks + 0.5F);
                Mouse.setCursorPosition(mouseX, mouseY);
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if(player == null)
            return;

        if(mc.currentScreen == null)
        {
            /* Handles rotating the yaw of player */
            if(controller.getRThumbStickXValue() != 0.0F || controller.getRThumbStickYValue() != 0.0F)
            {
                lastUse = 100;
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, 20.0F, 15.0F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float rotationYaw = turnEvent.getYawSpeed() * (controller.getRThumbStickXValue() > 0.0F ? 1 : -1) * Math.abs(controller.getRThumbStickXValue());
                    float rotationPitch = turnEvent.getPitchSpeed() * (controller.getRThumbStickYValue() > 0.0F ? 1 : -1) * Math.abs(controller.getRThumbStickYValue());
                    player.turn(rotationYaw, rotationPitch);
                }
            }
        }

        if(mc.currentScreen == null)
        {
            if(controller.isButtonPressed(Buttons.DPAD_DOWN))
            {
                lastUse = 100;
                dropCounter++;
            }
        }

        if(dropCounter > 40)
        {
            if (!mc.player.isSpectator())
            {
                mc.player.dropItem(true);
            }
            dropCounter = 0;
        }
        else if(dropCounter > 0 && !controller.isButtonPressed(Buttons.DPAD_DOWN))
        {
            if (!mc.player.isSpectator())
            {
                mc.player.dropItem(false);
            }
            dropCounter = 0;
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

        if(mc.player.capabilities.isFlying || mc.player.isRiding())
        {
            lastUse = 100;
            sneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            sneaking |= controller.isButtonPressed(Buttons.LEFT_THUMB_STICK);
            isFlying = true;
        }
        else if(isFlying)
        {
            sneaking = false;
            isFlying = false;
        }

        event.getMovementInput().sneak = sneaking;

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller)))
            {
                if(controller.getLThumbStickYValue() != 0.0F)
                {
                    lastUse = 100;
                    int dir = controller.getLThumbStickYValue() > 0.0F ? 1 : -1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * Math.abs(controller.getLThumbStickYValue());

                    if(event.getMovementInput().sneak)
                    {
                        event.getMovementInput().moveForward *= 0.3D;
                    }
                }

                if(controller.getLThumbStickXValue() != 0.0F)
                {
                    lastUse = 100;
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * Math.abs(controller.getLThumbStickXValue());

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

    public void handleButtonInput(Controller controller, int button, boolean state)
    {
        lastUse = 100;

        ControllerEvent.ButtonInput event = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        button = event.getModifiedButton();
        controller.setButtonState(button, state);

        Minecraft mc = Minecraft.getMinecraft();
        if(state)
        {
            if(button == Buttons.Y)
            {
                if(mc.currentScreen == null)
                {
                    if(mc.playerController.isRidingHorse())
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
                else if(mc.player != null)
                {
                    mc.player.closeScreen();
                }
            }
            else if(button == Buttons.LEFT_THUMB_STICK)
            {
                if(mc.currentScreen == null && mc.player != null && !mc.player.capabilities.isFlying && !mc.player.isRiding())
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
                else if(mc.currentScreen instanceof GuiContainerCreative)
                {
                    scrollCreativeTabs((GuiContainerCreative) mc.currentScreen, -1);
                }
            }
            else if(button == Buttons.RIGHT_BUMPER)
            {
                if(mc.currentScreen == null)
                {
                    mc.player.inventory.changeCurrentItem(-1);
                }
                else if(mc.currentScreen instanceof GuiContainerCreative)
                {
                    scrollCreativeTabs((GuiContainerCreative) mc.currentScreen, 1);
                }
            }
            else if(button == Buttons.A && mc.currentScreen != null)
            {
                invokeMouseClick(mc.currentScreen, 0);
            }
            else if(button == Buttons.X)
            {
                if(mc.currentScreen != null)
                {
                    invokeMouseClick(mc.currentScreen, 1);
                }
                else if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null)
                {
                    mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
            }
            else if(button == Buttons.B && mc.currentScreen != null && mc.player != null && mc.player.inventory.getItemStack().isEmpty())
            {
                invokeMouseClick(mc.currentScreen, 0);
            }
            else if(button == Buttons.DPAD_UP && mc.inGameHasFocus && mc.currentScreen == null)
            {
                cycleThirdPersonView();
            }
            else if(mc.player != null)
            {
                if(!mc.player.isHandActive() && mc.currentScreen == null)
                {
                    if(button == Buttons.RIGHT_TRIGGER)
                    {
                        mc.clickMouse();
                    }
                    else if(button == Buttons.LEFT_TRIGGER)
                    {
                        mc.rightClickMouse();
                    }
                    else if(button == Buttons.RIGHT_THUMB_STICK)
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

    private void scrollCreativeTabs(GuiContainerCreative creative, int dir)
    {
        lastUse = 100;

        try
        {
            Method method = ReflectionHelper.findMethod(GuiContainerCreative.class, "setCurrentCreativeTab", "func_147050_b", CreativeTabs.class);
            method.setAccessible(true);
            if(dir > 0)
            {
                if(creative.getSelectedTabIndex() < CreativeTabs.CREATIVE_TAB_ARRAY.length - 1)
                {
                    method.invoke(creative, CreativeTabs.CREATIVE_TAB_ARRAY[creative.getSelectedTabIndex() + 1]);
                }
            }
            else if(dir < 0)
            {
                if(creative.getSelectedTabIndex() > 0)
                {
                    method.invoke(creative, CreativeTabs.CREATIVE_TAB_ARRAY[creative.getSelectedTabIndex() - 1]);
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void moveMouseToClosestSlot(boolean moving, GuiScreen screen)
    {
        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof GuiContainer)
        {
            Minecraft mc = Minecraft.getMinecraft();
            GuiContainer guiContainer = (GuiContainer) screen;
            int guiLeft = (guiContainer.width - guiContainer.getXSize()) / 2;
            int guiTop = (guiContainer.height - guiContainer.getYSize()) / 2;
            int mouseX = targetMouseX * guiContainer.width / mc.displayWidth;
            int mouseY = guiContainer.height - targetMouseY * guiContainer.height / mc.displayHeight - 1;

            /* Finds the closest slot in the GUI within 14 pixels (inclusive) */
            Slot closestSlot = null;
            double closestDistance = -1.0;
            for(Slot slot : guiContainer.inventorySlots.inventorySlots)
            {
                int posX = guiLeft + slot.xPos + 8;
                int posY = guiTop + slot.yPos + 8;

                double distance = Math.sqrt(Math.pow(posX - mouseX, 2) + Math.pow(posY - mouseY, 2));
                if((closestDistance == -1.0 || distance < closestDistance) && distance <= 14.0)
                {
                    closestSlot = slot;
                    closestDistance = distance;
                }
            }

            if(closestSlot != null && (closestSlot.getHasStack() || !mc.player.inventory.getItemStack().isEmpty()))
            {
                int slotCenterX = guiLeft + closestSlot.xPos + 8;
                int slotCenterY = guiTop + closestSlot.yPos + 8;
                int realMouseX = (int) (slotCenterX / ((float) guiContainer.width / (float) mc.displayWidth));
                int realMouseY = (int) (-(slotCenterY + 1 - guiContainer.height) / ((float) guiContainer.width / (float) mc.displayWidth));
                int deltaX = targetMouseX - realMouseX;
                int deltaY = targetMouseY - realMouseY;
                int targetMouseXScaled = targetMouseX * guiContainer.width / mc.displayWidth;
                int targetMouseYScaled = guiContainer.height - targetMouseY * guiContainer.height / mc.displayHeight - 1;

                if(!moving)
                {
                    if(targetMouseXScaled != slotCenterX || targetMouseYScaled != slotCenterY)
                    {
                        targetMouseX -= deltaX * 0.5;
                        targetMouseY -= deltaY * 0.5;
                    }
                    else
                    {
                        mouseSpeedX = 0.0F;
                        mouseSpeedY = 0.0F;
                    }
                }

                mouseSpeedX *= 0.75F;
                mouseSpeedY *= 0.75F;
            }
            else
            {
                mouseSpeedX *= 0.1F;
                mouseSpeedY *= 0.1F;
            }
        }
        else
        {
            mouseSpeedX = 0.0F;
            mouseSpeedY = 0.0F;
        }
    }

    private void handleCreativeScrolling(GuiContainerCreative creative, Controller controller)
    {
        try
        {
            int i = (((GuiContainerCreative.ContainerCreative) creative.inventorySlots).itemList.size() + 9 - 1) / 9 - 5;
            int dir = 0;

            if(controller.isButtonPressed(Buttons.DPAD_UP) || controller.getRThumbStickYValue() >= 0.8F)
            {
                dir = 1;
            }
            else if(controller.isButtonPressed(Buttons.DPAD_DOWN) || controller.getRThumbStickYValue() <= -0.8F)
            {
                dir = -1;
            }

            Field field = ReflectionHelper.findField(GuiContainerCreative.class, "currentScroll", "field_147067_x");
            field.setAccessible(true);

            float currentScroll = field.getFloat(creative);
            currentScroll = (float) ((double) currentScroll - (double) dir / (double) i);
            currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
            field.setFloat(creative, currentScroll);
            ((GuiContainerCreative.ContainerCreative) creative.inventorySlots).scrollTo(currentScroll);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
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
                Field eventButton = ReflectionHelper.findField(GuiScreen.class, "eventButton", "field_146287_f");
                eventButton.setAccessible(true);
                eventButton.set(gui, button);

                Field lastMouseEvent = ReflectionHelper.findField(GuiScreen.class, "lastMouseEvent", "field_146288_g");
                lastMouseEvent.setAccessible(true);
                lastMouseEvent.set(gui, System.currentTimeMillis());

                Method mouseClicked = ReflectionHelper.findMethod(GuiScreen.class, "mouseClicked", "func_73864_a", int.class, int.class, int.class);
                mouseClicked.setAccessible(true);
                mouseClicked.invoke(gui, guiX, guiY, button);
            }
            catch(IllegalAccessException | InvocationTargetException e)
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
            int mouseX = Mouse.getX() * gui.width / mc.displayWidth;
            int mouseY = gui.height - Mouse.getY() * gui.height / mc.displayHeight - 1;

            try
            {
                Field eventButton = ReflectionHelper.findField(GuiScreen.class, "eventButton", "field_146287_f");
                eventButton.setAccessible(true);
                eventButton.set(gui, -1);

                //Resets the mouse straight away
                Method mouseReleased = ReflectionHelper.findMethod(GuiScreen.class, "mouseReleased", "func_146286_b", int.class, int.class, int.class);
                mouseReleased.setAccessible(true);
                mouseReleased.invoke(gui, mouseX, mouseY, button);
            }
            catch(IllegalAccessException | InvocationTargetException e)
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
        if(controller != null)
        {
            if(controller.isButtonPressed(Buttons.RIGHT_TRIGGER))
            {
                isLeftClicking = true;
            }
        }
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
        if(controller != null)
        {
            if(controller.isButtonPressed(Buttons.LEFT_TRIGGER))
            {
                isRightClicking = true;
            }
        }
        return isRightClicking;
    }

    /**
     * Used in order to fix the quick move check in inventories. This method is linked via ASM.
     */
    public static boolean canQuickMove()
    {
        boolean isSneaking = (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            if(controller.isButtonPressed(Buttons.B))
            {
                isSneaking = true;
            }
        }
        return isSneaking;
    }
}

//SCUFFED PS3
//X = 3
//A = 2
//B = 1
//Y = 0

//Official PS3
//X = 0
//A = 1
//B = 2
//Y = 3
//LEFT_BUMPER = 4
//RIGHT_BUMPER = 5
//LEFT_TRIGGER = 6
//RIGHT_TRIGGER = 7
//SELECT = 8
//START = 9
//LEFT_THUMB_STICK = 10
//RIGHT_THUMB_STICK = 11
//HOME = 12
//TOUCH_PAD = 13

//Official PS1
//X = 3
//A = 2
//B = 1
//Y = 0
//LEFT_BUMPER = 6
//RIGHT_BUMPER = 7
//LEFT_TRIGGER = 4
//RIGHT_TRIGGER = 5
