package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.event.ControllerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerInput
{
    public static int lastUse = 0;

    private boolean keyboardSneaking = false;
    private boolean sneaking = false;
    private boolean isFlying = false;

    private float prevXAxis;
    private float prevYAxis;
    private double prevTargetMouseX;
    private double prevTargetMouseY;
    private double targetMouseX;
    private double targetMouseY;
    private double mouseSpeedX;
    private double mouseSpeedY;

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

            Minecraft mc = Minecraft.getInstance();
            if(mc.mouseHelper.isMouseGrabbed())
                return;

            if(mc.currentScreen == null || mc.currentScreen instanceof ControllerLayoutScreen)
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
                    prevTargetMouseX = targetMouseX = mc.mouseHelper.getMouseX();
                    prevTargetMouseY = targetMouseY = mc.mouseHelper.getMouseY();
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
                targetMouseY -= 30 * mouseSpeedY;
            }

            prevXAxis = controller.getLThumbStickXValue();
            prevYAxis = controller.getLThumbStickYValue();

            this.moveMouseToClosestSlot(moving, mc.currentScreen);

            if(mc.currentScreen instanceof CreativeScreen)
            {
                this.handleCreativeScrolling((CreativeScreen) mc.currentScreen, controller);
            }
        }
    }

    @SubscribeEvent
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getInstance();
        if(mc.currentScreen != null && (targetMouseX != prevTargetMouseX || targetMouseY != prevTargetMouseY))
        {
            if(!(mc.currentScreen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
                double mouseX = (prevTargetMouseX + (targetMouseX - prevTargetMouseX) * partialTicks + 0.5);
                double mouseY = (prevTargetMouseY + (targetMouseY - prevTargetMouseY) * partialTicks + 0.5);
                GLFW.glfwSetCursorPos(mc.mainWindow.getHandle(), mouseX, mouseY);
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

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
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
                    float rotationPitch = turnEvent.getPitchSpeed() * (controller.getRThumbStickYValue() > 0.0F ? -1 : 1) * Math.abs(controller.getRThumbStickYValue());
                    player.rotateTowards(rotationYaw, rotationPitch);
                }
            }
        }

        if(mc.currentScreen == null)
        {
            if(ButtonBindings.DROP_ITEM.isButtonDown())
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
        else if(dropCounter > 0 && !ButtonBindings.DROP_ITEM.isButtonDown())
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
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        Minecraft mc = Minecraft.getInstance();

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

        if(mc.player.abilities.isFlying || mc.player.isPassenger())
        {
            lastUse = 100;
            sneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            sneaking |= ButtonBindings.SNEAK.isButtonDown();
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

            if(ButtonBindings.JUMP.isButtonDown())
            {
                event.getMovementInput().jump = true;
            }
        }

        if(ButtonBindings.USE_ITEM.isButtonDown() && mc.rightClickDelayTimer == 0 && !mc.player.isHandActive())
        {
            mc.rightClickMouse();
        }
    }

    public void handleButtonInput(Controller controller, int button, boolean state)
    {
        if(Minecraft.getInstance().currentScreen instanceof ControllerLayoutScreen)
        {
            return;
        }

        lastUse = 100;

        ControllerEvent.ButtonInput eventInput = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(eventInput))
            return;

        button = eventInput.getModifiedButton();
        ButtonBinding.setButtonState(button, state);

        ControllerEvent.Button event = new ControllerEvent.Button(controller);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        Minecraft mc = Minecraft.getInstance();
        if(state)
        {
            if(mc.currentScreen == null)
            {
                if(ButtonBindings.INVENTORY.isButtonPressed())
                {
                    if(mc.playerController.isRidingHorse())
                    {
                        mc.player.sendHorseInventory();
                    }
                    else
                    {
                        mc.getTutorial().openInventory();
                        mc.displayGuiScreen(new InventoryScreen(mc.player));
                    }
                    prevTargetMouseX = targetMouseX = mc.mouseHelper.getMouseX();
                    prevTargetMouseY = targetMouseY = mc.mouseHelper.getMouseY();
                }
                else if(ButtonBindings.SNEAK.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.abilities.isFlying && !mc.player.isPassenger())
                    {
                        sneaking = !sneaking;
                    }
                }
                else if(ButtonBindings.SCROLL_RIGHT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(-1);
                    }
                }
                else if(ButtonBindings.SCROLL_LEFT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.inventory.changeCurrentItem(1);
                    }
                }
                else if(ButtonBindings.SWAP_HANDS.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null)
                    {

                        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
                    }
                }
                else if(ButtonBindings.TOGGLE_PERSPECTIVE.isButtonPressed() && mc.mouseHelper.isMouseGrabbed())
                {
                    cycleThirdPersonView();
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.displayInGameMenu(false);
                    }
                }
                else if(mc.player != null && !mc.player.isHandActive())
                {
                    if(ButtonBindings.ATTACK.isButtonPressed())
                    {
                        mc.clickMouse();
                    }
                    else if(ButtonBindings.USE_ITEM.isButtonPressed())
                    {
                        mc.rightClickMouse();
                    }
                    else if(ButtonBindings.PICK_BLOCK.isButtonPressed())
                    {
                        mc.middleClickMouse();
                    }
                }
            }
            else
            {
                if(ButtonBindings.INVENTORY.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.closeScreen();
                    }
                }
                else if(ButtonBindings.SCROLL_RIGHT.isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, 1);
                    }
                }
                else if(ButtonBindings.SCROLL_LEFT.isButtonPressed())
                {
                    if(mc.currentScreen instanceof CreativeScreen)
                    {
                        scrollCreativeTabs((CreativeScreen) mc.currentScreen, -1);
                    }
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.currentScreen instanceof IngameMenuScreen)
                    {
                        mc.displayGuiScreen(null);
                    }
                }
                else if(button == Buttons.A)
                {
                    invokeMouseClick(mc.currentScreen, 0);
                }
                else if(button == Buttons.X)
                {
                    invokeMouseClick(mc.currentScreen, 1);
                }
                else if(button == Buttons.B && mc.player != null && mc.player.inventory.getItemStack().isEmpty())
                {
                    invokeMouseClick(mc.currentScreen, 0);
                }
            }
        }
        else
        {
            if(mc.currentScreen == null)
            {

            }
            else
            {
                if(button == Buttons.A)
                {
                    invokeMouseReleased(mc.currentScreen, 0);
                }
                else if(button == Buttons.X)
                {
                    invokeMouseReleased(mc.currentScreen, 1);
                }
            }
        }
    }

    /**
     * Cycles the third person view. Minecraft doesn't have this code in a convenient method.
     */
    private void cycleThirdPersonView()
    {
        Minecraft mc = Minecraft.getInstance();

        mc.gameSettings.thirdPersonView++;
        if(mc.gameSettings.thirdPersonView > 2)
        {
            mc.gameSettings.thirdPersonView = 0;
        }

        if(mc.gameSettings.thirdPersonView == 0)
        {
            mc.gameRenderer.loadEntityShader(mc.getRenderViewEntity());
        }
        else if(mc.gameSettings.thirdPersonView == 1)
        {
            mc.gameRenderer.loadEntityShader(null);
        }
    }

    private void scrollCreativeTabs(CreativeScreen creative, int dir)
    {
        lastUse = 100;

        try
        {
            Method method = ObfuscationReflectionHelper.findMethod(CreativeScreen.class, "func_147050_b", ItemGroup.class);
            method.setAccessible(true);
            if(dir > 0)
            {
                if(creative.getSelectedTabIndex() < ItemGroup.GROUPS.length - 1)
                {
                    method.invoke(creative, ItemGroup.GROUPS[creative.getSelectedTabIndex() + 1]);
                }
            }
            else if(dir < 0)
            {
                if(creative.getSelectedTabIndex() > 0)
                {
                    method.invoke(creative, ItemGroup.GROUPS[creative.getSelectedTabIndex() - 1]);
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void moveMouseToClosestSlot(boolean moving, Screen screen)
    {
        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof ContainerScreen)
        {
            Minecraft mc = Minecraft.getInstance();
            ContainerScreen guiContainer = (ContainerScreen) screen;
            int guiLeft = (guiContainer.width - guiContainer.getXSize()) / 2;
            int guiTop = (guiContainer.height - guiContainer.getYSize()) / 2;
            double mouseX = targetMouseX * guiContainer.width / mc.mainWindow.getWidth(); //TODO needs testing. may need to change
            double mouseY = guiContainer.height - targetMouseY * guiContainer.height / mc.mainWindow.getHeight() - 1;

            /* Finds the closest slot in the GUI within 14 pixels (inclusive) */
            Slot closestSlot = null;
            double closestDistance = -1.0;
            for(Slot slot : guiContainer.getContainer().inventorySlots)
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
                double realMouseX = (slotCenterX / ((float) guiContainer.width / (float) mc.mainWindow.getWidth())); //TODO test this! may need changing
                double realMouseY = (-(slotCenterY + 1 - guiContainer.height) / ((float) guiContainer.width / (float) mc.mainWindow.getWidth()));
                double deltaX = targetMouseX - realMouseX;
                double deltaY = targetMouseY - realMouseY;
                double targetMouseXScaled = targetMouseX * guiContainer.width / mc.mainWindow.getWidth();
                double targetMouseYScaled = guiContainer.height - targetMouseY * guiContainer.height / mc.mainWindow.getHeight() - 1;

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

    private void handleCreativeScrolling(CreativeScreen creative, Controller controller)
    {
        try
        {
            int i = (creative.getContainer().itemList.size() + 9 - 1) / 9 - 5;
            int dir = 0;

            if(controller.getState().dpadUp || controller.getRThumbStickYValue() >= 0.8F)
            {
                dir = 1;
            }
            else if(controller.getState().dpadDown || controller.getRThumbStickYValue() <= -0.8F)
            {
                dir = -1;
            }

            Field field = ObfuscationReflectionHelper.findField(CreativeScreen.class, "field_147067_x");
            field.setAccessible(true);

            float currentScroll = field.getFloat(creative);
            currentScroll = (float) ((double) currentScroll - (double) dir / (double) i);
            currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
            field.setFloat(creative, currentScroll);
            creative.getContainer().scrollTo(currentScroll);
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
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseClick(Screen screen, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            MouseHelper helper = mc.mouseHelper;
            double mouseX = helper.getMouseX() * (double) mc.mainWindow.getScaledWidth() / (double) mc.mainWindow.getWidth();
            double mouseY = helper.getMouseY() * (double) mc.mainWindow.getScaledHeight() / (double) mc.mainWindow.getHeight();

            try
            {
                Field eventButton = ObfuscationReflectionHelper.findField(MouseHelper.class, "field_198042_g");
                eventButton.setAccessible(true);
                eventButton.set(mc.mouseHelper, button);

                Field lastMouseEvent = ObfuscationReflectionHelper.findField(MouseHelper.class, "field_198045_j");
                lastMouseEvent.setAccessible(true);
                lastMouseEvent.set(mc.mouseHelper, NativeUtil.func_216394_b());

                Screen.wrapScreenError(() -> {
                    boolean cancelled = ForgeHooksClient.onGuiMouseClickedPre(screen, mouseX, mouseY, button);
                    if (!cancelled) {
                        cancelled = screen.mouseClicked(mouseX, mouseY, button);
                    }
                    if (!cancelled) {
                        ForgeHooksClient.onGuiMouseClickedPost(screen, mouseX, mouseY, button);
                    }
                }, "mouseClicked event handler", screen.getClass().getCanonicalName());
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Invokes a mouse released in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseReleased(Screen screen, int button)
    {
        Minecraft mc = Minecraft.getInstance();
        if(screen != null)
        {
            MouseHelper helper = mc.mouseHelper;
            double mouseX = helper.getMouseX() * (double) mc.mainWindow.getScaledWidth() / (double) mc.mainWindow.getWidth();
            double mouseY = helper.getMouseY() * (double) mc.mainWindow.getScaledHeight() / (double) mc.mainWindow.getHeight();

            try
            {
                Field eventButton = ObfuscationReflectionHelper.findField(MouseHelper.class, "field_198042_g");
                eventButton.setAccessible(true);
                eventButton.set(mc.mouseHelper, -1);

                Screen.wrapScreenError(() -> {
                    boolean cancelled = ForgeHooksClient.onGuiMouseReleasedPre(screen, mouseX, mouseY, button);
                    if (!cancelled) {
                        cancelled = screen.mouseReleased(mouseX, mouseY, button);
                    }
                    if (!cancelled) {
                        ForgeHooksClient.onGuiMouseReleasedPost(screen, mouseX, mouseY, button);
                    }
                }, "mouseReleased event handler", screen.getClass().getCanonicalName());
            }
            catch(IllegalAccessException e)
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
        return mc.currentScreen == null && isLeftClicking && mc.mouseHelper.isMouseGrabbed();
    }

    /**
     * Used in order to fix actions like eating or pulling bow back. This method is linked via ASM.
     */
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
