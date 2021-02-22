package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.ControllerLayoutScreen;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.GatherNavigationPointsEvent;
import com.mrcrayfish.controllable.mixin.client.CreativeScreenMixin;
import com.mrcrayfish.controllable.mixin.client.RecipeBookGuiMixin;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class ControllerInput
{
    private static final ResourceLocation CURSOR_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/cursor.png");

    private int lastUse = 0;
    private boolean keyboardSneaking = false;
    private boolean sneaking = false;
    private boolean isFlying = false;
    private boolean nearSlot = false;
    private boolean moving = false;
    private boolean preventReset;
    private double virtualMouseX;
    private double virtualMouseY;
    private int prevTargetMouseX;
    private int prevTargetMouseY;
    private int targetMouseX;
    private int targetMouseY;
    private double mouseSpeedX;
    private double mouseSpeedY;
    private int lastRealMouseX;
    private int lastRealMouseY;
    private boolean moved;
    private float targetPitch;
    private float targetYaw;

    private int dropCounter = -1;

    public double getVirtualMouseX()
    {
        return this.virtualMouseX;
    }

    public double getVirtualMouseY()
    {
        return this.virtualMouseY;
    }

    private void setControllerInUse()
    {
        this.lastUse = 100;
    }

    public boolean isControllerInUse()
    {
        return this.lastUse > 0;
    }

    public int getLastUse()
    {
        return this.lastUse;
    }

    public void resetLastUse()
    {
        if(!this.preventReset)
        {
            this.lastUse = 0;
        }
        this.preventReset = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            if(Mouse.getX() != this.lastRealMouseX || Mouse.getY() != this.lastRealMouseY)
            {
                this.resetLastUse();
            }
            this.lastRealMouseX = Mouse.getX();
            this.lastRealMouseY = Mouse.getY();

            this.prevTargetMouseX = this.targetMouseX;
            this.prevTargetMouseY = this.targetMouseY;

            if(this.lastUse > 0)
            {
                this.lastUse--;
            }

            Controller controller = Controllable.getController();
            if(controller == null)
                return;

            if((Math.abs(controller.getLTriggerValue()) > 0.5F || Math.abs(controller.getRTriggerValue()) > 0.5F) && !(Minecraft.getMinecraft().currentScreen instanceof ControllerLayoutScreen))
            {
                this.setControllerInUse();
            }

            Minecraft mc = Minecraft.getMinecraft();
            if(Mouse.isGrabbed())
                return;

            if(mc.currentScreen == null || mc.currentScreen instanceof ControllerLayoutScreen)
                return;

            float deadZone = (float) Math.min(1.0F, Controllable.getOptions().getDeadZone() + 0.25F);

            /* Only need to run code if left thumb stick has input */
            boolean lastMoving = this.moving;
            this.moving = Math.abs(controller.getLThumbStickXValue()) >= deadZone || Math.abs(controller.getLThumbStickYValue()) >= deadZone;
            if(this.moving)
            {
                /* Updates the target mouse position when the initial thumb stick movement is
                 * detected. This fixes an issue when the user moves the cursor with the mouse then
                 * switching back to controller, the cursor would jump to old target mouse position. */
                if(!lastMoving)
                {
                    double mouseX = Mouse.getX();
                    double mouseY = Mouse.getY();
                    if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse())
                    {
                        mouseX = this.virtualMouseX;
                        mouseY = this.virtualMouseY;
                    }
                    this.prevTargetMouseX = this.targetMouseX = (int) mouseX;
                    this.prevTargetMouseY = this.targetMouseY = (int) mouseY;
                }

                float xAxis = controller.getLThumbStickXValue();
                this.mouseSpeedX = Math.abs(xAxis) >= deadZone ? Math.signum(xAxis) * (Math.abs(xAxis) - deadZone) / (1.0F - deadZone) : 0.0F;

                float yAxis = controller.getLThumbStickYValue();
                this.mouseSpeedY = Math.abs(yAxis) >= deadZone ? Math.signum(yAxis) * (Math.abs(yAxis) - deadZone) / (1.0F - deadZone) : 0.0F;

                this.setControllerInUse();
            }

            if(this.lastUse <= 0)
            {
                this.mouseSpeedX = 0F;
                this.mouseSpeedY = 0F;
                return;
            }

            if(Math.abs(this.mouseSpeedX) > 0F || Math.abs(this.mouseSpeedY) > 0F)
            {
                ScaledResolution resolution = new ScaledResolution(mc);
                double mouseSpeed = Controllable.getOptions().getMouseSpeed() * resolution.getScaleFactor();

                // When hovering over slots, slows down the mouse speed to make it easier
                if(mc.currentScreen instanceof GuiContainer)
                {
                    GuiContainer screen = (GuiContainer) mc.currentScreen;
                    if(screen.getSlotUnderMouse() != null)
                    {
                        mouseSpeed *= 0.5;
                    }
                }

                List<GuiButton> widgets = new ArrayList<>(mc.currentScreen.buttonList);
                if(mc.currentScreen instanceof IRecipeShownListener)
                {
                    GuiRecipeBook recipeBook = ((IRecipeShownListener) mc.currentScreen).func_194310_f();
                    if(recipeBook.isVisible())
                    {
                        widgets.add(((RecipeBookGuiMixin) recipeBook).getToggleRecipesBtn());
                        widgets.addAll(((RecipeBookGuiMixin) recipeBook).getRecipeTabs());
                        RecipeBookPage recipeBookPage = ((RecipeBookGuiMixin) recipeBook).getRecipeBookPage();
                        widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).getButtons());
                        widgets.add(((RecipeBookPageAccessor) recipeBookPage).getForwardButton());
                        widgets.add(((RecipeBookPageAccessor) recipeBookPage).getBackButton());
                    }
                }
                GuiButton hoveredListener = widgets.stream().filter(o -> o != null && o.isMouseOver()).findFirst().orElse(null);
                if(hoveredListener != null)
                {
                    mouseSpeed *= 0.6;
                }

                this.targetMouseX += mouseSpeed * this.mouseSpeedX;
                this.targetMouseX = MathHelper.clamp(this.targetMouseX, 0, mc.displayWidth);
                this.targetMouseY += mouseSpeed * this.mouseSpeedY;
                this.targetMouseY = MathHelper.clamp(this.targetMouseY, 0, mc.displayHeight);
                this.setControllerInUse();
                this.moved = true;
            }

            this.moveMouseToClosestSlot(this.moving, mc.currentScreen);

            if(mc.currentScreen instanceof GuiContainerCreative)
            {
                this.handleCreativeScrolling((GuiContainerCreative) mc.currentScreen, controller);
            }

            /*if(Controllable.getOptions().isVirtualMouse() && (this.targetMouseX != this.prevTargetMouseX || this.targetMouseY != this.prevTargetMouseY))
            {
                this.performMouseDrag(this.virtualMouseX, this.virtualMouseY, this.targetMouseX - this.prevTargetMouseX, this.targetMouseY - this.prevTargetMouseY);
            }*/
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onScreenInit(GuiOpenEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen == null)
        {
            this.nearSlot = false;
            this.moved = false;
            this.mouseSpeedX = 0.0;
            this.mouseSpeedY = 0.0;
            this.virtualMouseX = this.targetMouseX = this.prevTargetMouseX = (int) (mc.displayWidth / 2F);
            this.virtualMouseY = this.targetMouseY = this.prevTargetMouseY = (int) (mc.displayHeight / 2F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        /* Makes the cursor movement appear smooth between ticks. This will only run if the target
         * mouse position is different to the previous tick's position. This allows for the mouse
         * to still be used as input. */
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen != null && (this.targetMouseX != this.prevTargetMouseX || this.targetMouseY != this.prevTargetMouseY))
        {
            if(!(mc.currentScreen instanceof ControllerLayoutScreen))
            {
                float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
                double mouseX = (this.prevTargetMouseX + (this.targetMouseX - this.prevTargetMouseX) * partialTicks + 0.5);
                double mouseY = (this.prevTargetMouseY + (this.targetMouseY - this.prevTargetMouseY) * partialTicks + 0.5);
                this.setMousePosition(mouseX, mouseY);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && lastUse > 0)
        {
            GlStateManager.pushMatrix();
            {
                CursorType type = Controllable.getOptions().getCursorType();
                Minecraft minecraft = event.getGui().mc;
                if(minecraft.player == null || (minecraft.player.inventory.getItemStack().isEmpty() || type == CursorType.CONSOLE))
                {
                    double mouseX = (this.prevTargetMouseX + (this.targetMouseX - this.prevTargetMouseX) * Minecraft.getMinecraft().getRenderPartialTicks());
                    double mouseY = (this.prevTargetMouseY + (this.targetMouseY - this.prevTargetMouseY) * Minecraft.getMinecraft().getRenderPartialTicks());
                    ScaledResolution resolution = new ScaledResolution(minecraft);
                    GlStateManager.translate(mouseX / resolution.getScaleFactor(), mouseY / resolution.getScaleFactor(), 500);
                    GlStateManager.color(1.0F, 1.0F, 1.0F);
                    GlStateManager.enableBlend();
                    GlStateManager.enableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    event.getGui().mc.getTextureManager().bindTexture(CURSOR_TEXTURE);
                    if(type == CursorType.CONSOLE)
                    {
                        GlStateManager.scale(0.5, 0.5, 0.5);
                    }
                    GuiScreen.drawModalRectWithCustomSizedTexture(-8, -8, this.nearSlot ? 16 : 0, type.ordinal() * 16, 16, 16, 32, CursorType.values().length * 16);
                    GlStateManager.disableBlend();
                }
            }
            GlStateManager.popMatrix();
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
        ScaledResolution resolution = new ScaledResolution(mc);
        double mouseX = this.virtualMouseX * (double) resolution.getScaledWidth() / (double) mc.displayWidth;
        double mouseY = this.virtualMouseY * (double) resolution.getScaledHeight() / (double) mc.displayHeight;
        if(mc.currentScreen != null && this.lastUse > 0)
        {
            //Yeah it's not possible to find lists in the GUI unlike 1.16
            /*GuiButton hoveredListener = mc.currentScreen.buttonList.stream().filter(o -> o.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
            if(hoveredListener instanceof GuiSlot)
            {
                this.handleListScrolling((AbstractList) hoveredListener, controller);
            }*/
        }

        EntityPlayer player = mc.player;
        if(player == null)
            return;

        if(mc.currentScreen == null && (this.targetYaw != 0F || this.targetPitch != 0F))
        {
            float elapsedTicks = Minecraft.getMinecraft().getTickLength();
            player.turn((this.targetYaw / 0.15F) * elapsedTicks, (this.targetPitch / 0.15F) * (Controllable.getOptions().isInvertLook() ? -1 : 1) * elapsedTicks);
            if(player.getRidingEntity() != null)
            {
                player.getRidingEntity().applyOrientationToEntity(player);
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
            return;

        this.targetYaw = 0F;
        this.targetPitch = 0F;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if(player == null)
            return;

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(mc.currentScreen == null)
        {
            float deadZone = (float) Controllable.getOptions().getDeadZone();

            /* Handles rotating the yaw of player */
            if(Math.abs(controller.getRThumbStickXValue()) >= deadZone)
            {
                this.setControllerInUse();
                double rotationSpeed = Controllable.getOptions().getRotationSpeed();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrimX = (controller.getRThumbStickXValue() > 0 ? 1 : -1) * deadZone;
                    this.targetYaw = (turnEvent.getYawSpeed() * (controller.getRThumbStickXValue() - deadZoneTrimX) / (1.0F - deadZone)) * 0.33F;
                }
            }

            if(Math.abs(controller.getRThumbStickYValue()) >= deadZone)
            {
                this.setControllerInUse();
                double rotationSpeed = Controllable.getOptions().getRotationSpeed();
                ControllerEvent.Turn turnEvent = new ControllerEvent.Turn(controller, (float) rotationSpeed, (float) rotationSpeed * 0.75F);
                if(!MinecraftForge.EVENT_BUS.post(turnEvent))
                {
                    float deadZoneTrimY = (controller.getRThumbStickYValue() > 0 ? 1 : -1) * deadZone;
                    this.targetPitch = (turnEvent.getPitchSpeed() * (-controller.getRThumbStickYValue() - deadZoneTrimY) / (1.0F - deadZone)) * 0.33F;
                }
            }
        }

        if(mc.currentScreen == null)
        {
            if(ButtonBindings.DROP_ITEM.isButtonDown())
            {
                this.setControllerInUse();
                this.dropCounter++;
            }
        }

        if(this.dropCounter > 20)
        {
            if (!mc.player.isSpectator())
            {
                mc.player.dropItem(true);
            }
            this.dropCounter = 0;
        }
        else if(this.dropCounter > 0 && !ButtonBindings.DROP_ITEM.isButtonDown())
        {
            if (!mc.player.isSpectator())
            {
                mc.player.dropItem(false);
            }
            this.dropCounter = 0;
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

        if(this.keyboardSneaking && !mc.gameSettings.keyBindSneak.isKeyDown())
        {
            this.sneaking = false;
            this.keyboardSneaking = false;
        }

        if(mc.gameSettings.keyBindSneak.isKeyDown())
        {
            this.sneaking = true;
            this.keyboardSneaking = true;
        }

        if(mc.player.capabilities.isFlying || mc.player.isRiding())
        {
            this.sneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            this.sneaking |= ButtonBindings.SNEAK.isButtonDown();
            if(ButtonBindings.SNEAK.isButtonDown())
            {
                this.setControllerInUse();
            }
            this.isFlying = true;
        }
        else if(this.isFlying)
        {
            this.sneaking = false;
            this.isFlying = false;
        }

        event.getMovementInput().sneak = this.sneaking;

        if(mc.currentScreen == null)
        {
            if(!MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller)))
            {
                float deadZone = (float) Controllable.getOptions().getDeadZone();

                if(Math.abs(controller.getLThumbStickYValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickYValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().forwardKeyDown = dir > 0;
                    event.getMovementInput().backKeyDown = dir < 0;
                    event.getMovementInput().moveForward = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickYValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

                    if(event.getMovementInput().sneak)
                    {
                        event.getMovementInput().moveForward *= 0.3D;
                    }
                }

                if(player.getRidingEntity() instanceof EntityBoat)
                {
                    deadZone = 0.5F;
                }

                if(Math.abs(controller.getLThumbStickXValue()) >= deadZone)
                {
                    this.setControllerInUse();
                    int dir = controller.getLThumbStickXValue() > 0.0F ? -1 : 1;
                    event.getMovementInput().rightKeyDown = dir < 0;
                    event.getMovementInput().leftKeyDown = dir > 0;
                    event.getMovementInput().moveStrafe = dir * MathHelper.clamp((Math.abs(controller.getLThumbStickXValue()) - deadZone) / (1.0F - deadZone), 0.0F, 1.0F);

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
        this.setControllerInUse();

        ControllerEvent.ButtonInput eventInput = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(eventInput))
            return;

        button = eventInput.getModifiedButton();
        ButtonBinding.setButtonState(button, state);

        ControllerEvent.Button event = new ControllerEvent.Button(controller);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if(state)
        {
            if(ButtonBindings.FULLSCREEN.isButtonPressed())
            {
                mc.toggleFullscreen();
            }
            else if(ButtonBindings.SCREENSHOT.isButtonPressed())
            {
                if(mc.world != null)
                {
                    ScreenShotHelper.saveScreenshot(mc.gameDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
                }
            }
            else if(mc.currentScreen == null)
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
                        mc.displayGuiScreen(new GuiInventory(mc.player));
                    }
                }
                else if(ButtonBindings.SPRINT.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.player.setSprinting(true);
                    }
                }
                else if(ButtonBindings.SNEAK.isButtonPressed())
                {
                    if(mc.player != null && !mc.player.capabilities.isFlying && !mc.player.isRiding())
                    {
                        this.sneaking = !this.sneaking;
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
                        mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }
                else if(ButtonBindings.TOGGLE_PERSPECTIVE.isButtonPressed() && Mouse.isGrabbed())
                {
                    cycleThirdPersonView();
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.displayInGameMenu();
                    }
                }
                else if(ButtonBindings.ADVANCEMENTS.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.displayGuiScreen(new GuiScreenAdvancements(mc.player.connection.getAdvancementManager()));
                    }
                }
                else if(ButtonBindings.CINEMATIC_CAMERA.isButtonPressed())
                {
                    if(mc.player != null)
                    {
                        mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera;
                    }
                }
                else if(ButtonBindings.DEBUG_INFO.isButtonPressed())
                {
                    mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
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
                else if(ButtonBindings.PREVIOUS_CREATIVE_TAB.isButtonPressed())
                {
                    if(mc.currentScreen instanceof GuiContainerCreative)
                    {
                        this.scrollCreativeTabs((GuiContainerCreative) mc.currentScreen, 1);
                        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    else if(mc.currentScreen instanceof IRecipeShownListener)
                    {
                        IRecipeShownListener recipeShownListener = (IRecipeShownListener) mc.currentScreen;
                        this.scrollRecipePage(recipeShownListener.func_194310_f(), 1);
                    }
                }
                else if(ButtonBindings.NEXT_CREATIVE_TAB.isButtonPressed())
                {
                    if(mc.currentScreen instanceof GuiContainerCreative)
                    {
                        this.scrollCreativeTabs((GuiContainerCreative) mc.currentScreen, -1);
                        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    else if(mc.currentScreen instanceof IRecipeShownListener)
                    {
                        IRecipeShownListener recipeShownListener = (IRecipeShownListener) mc.currentScreen;
                        this.scrollRecipePage(recipeShownListener.func_194310_f(), -1);
                    }
                }
                else if(ButtonBindings.NEXT_RECIPE_TAB.isButtonPressed())
                {
                    if(mc.currentScreen instanceof IRecipeShownListener)
                    {
                        IRecipeShownListener recipeShownListener = (IRecipeShownListener) mc.currentScreen;
                        this.scrollRecipeTab(recipeShownListener.func_194310_f(), -1);
                    }
                }
                else if(ButtonBindings.PREVIOUS_RECIPE_TAB.isButtonPressed())
                {
                    if(mc.currentScreen instanceof IRecipeShownListener)
                    {
                        IRecipeShownListener recipeShownListener = (IRecipeShownListener) mc.currentScreen;
                        this.scrollRecipeTab(recipeShownListener.func_194310_f(), 1);
                    }
                }
                else if(ButtonBindings.PAUSE_GAME.isButtonPressed())
                {
                    if(mc.currentScreen instanceof GuiIngameMenu)
                    {
                        mc.displayGuiScreen(null);
                    }
                }
                else if(ButtonBindings.NAVIGATE_UP.isButtonPressed())
                {
                    this.navigateMouse(mc.currentScreen, Navigate.UP);
                }
                else if(ButtonBindings.NAVIGATE_DOWN.isButtonPressed())
                {
                    this.navigateMouse(mc.currentScreen, Navigate.DOWN);
                }
                else if(ButtonBindings.NAVIGATE_LEFT.isButtonPressed())
                {
                    this.navigateMouse(mc.currentScreen, Navigate.LEFT);
                }
                else if(ButtonBindings.NAVIGATE_RIGHT.isButtonPressed())
                {
                    this.navigateMouse(mc.currentScreen, Navigate.RIGHT);
                }
                else if(button == ButtonBindings.PICKUP_ITEM.getButton())
                {
                    invokeMouseClick(mc.currentScreen, 0);

                    if(Controllable.getOptions().isQuickCraft())
                    {
                        this.craftRecipeBookItem();
                    }
                }
                else if(button == ButtonBindings.SPLIT_STACK.getButton())
                {
                    invokeMouseClick(mc.currentScreen, 1);
                }
                else if(button == ButtonBindings.QUICK_MOVE.getButton() && mc.player != null)
                {
                    if(mc.player.inventory.getItemStack().isEmpty())
                    {
                        invokeMouseClick(mc.currentScreen, 0);
                    }
                    else
                    {
                        invokeMouseReleased(mc.currentScreen, 1);
                    }
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
                if(button == ButtonBindings.PICKUP_ITEM.getButton())
                {
                    invokeMouseReleased(mc.currentScreen, 0);
                }
                else if(button == ButtonBindings.SPLIT_STACK.getButton())
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
        Minecraft mc = Minecraft.getMinecraft();
        int thirdPersonView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = (thirdPersonView + 1) % 3;
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
        this.setControllerInUse();

        try
        {
            Method method = ObfuscationReflectionHelper.findMethod(GuiContainerCreative.class, "func_147050_b", void.class, CreativeTabs.class);
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

    private void scrollRecipeTab(GuiRecipeBook recipeBook, int dir)
    {
        RecipeBookGuiMixin recipeBookMixin = ((RecipeBookGuiMixin) recipeBook);
        GuiButtonRecipeTab currentTab = recipeBookMixin.getCurrentTab();
        List<GuiButtonRecipeTab> tabs = recipeBookMixin.getRecipeTabs();
        int nextTabIndex = tabs.indexOf(currentTab) + dir;
        if(nextTabIndex >= 0 && nextTabIndex < tabs.size())
        {
            GuiButtonRecipeTab newTab = tabs.get(nextTabIndex);
            currentTab.setStateTriggered(false);
            recipeBookMixin.setCurrentTab(newTab);
            newTab.setStateTriggered(true);
            recipeBookMixin.invokeUpdateCollections(true);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void scrollRecipePage(GuiRecipeBook recipeBook, int dir)
    {
        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookGuiMixin) recipeBook).getRecipeBookPage();
        if(dir > 0 && page.getForwardButton().visible || dir < 0 && page.getBackButton().visible)
        {
            int currentPage = page.getCurrentPage();
            page.setCurrentPage(currentPage + dir);
            page.invokeUpdateButtonsForPage();
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void navigateMouse(GuiScreen screen, Navigate navigate)
    {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int mouseX = (int) (this.targetMouseX * (double) resolution.getScaledWidth() / (double) mc.displayWidth);
        int mouseY = (int) (this.targetMouseY * (double) resolution.getScaledHeight() / (double) mc.displayHeight);

        List<NavigationPoint> points = this.gatherNavigationPoints(screen);

        // Gather any extra navigation points from event
        GatherNavigationPointsEvent event = new GatherNavigationPointsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        points.addAll(event.getPoints());

        // Get only the points that are in the target direction
        List<NavigationPoint> targetPoints = points.stream().filter(point -> navigate.getPredicate().test(point, mouseX, mouseY)).collect(Collectors.toList());
        if(targetPoints.isEmpty())
            return;

        Vec3d mousePos = new Vec3d(mouseX, mouseY, 0);
        Optional<NavigationPoint> minimumPointOptional = targetPoints.stream().min(navigate.getMinComparator(mouseX, mouseY));
        double minimumDelta = navigate.getKeyExtractor().apply(minimumPointOptional.get(), mousePos) + 10;
        Optional<NavigationPoint> targetPointOptional = targetPoints.stream().filter(point -> navigate.getKeyExtractor().apply(point, mousePos) <= minimumDelta).min(Comparator.comparing(p -> p.distanceTo(mouseX, mouseY)));
        if(targetPointOptional.isPresent())
        {
            //this.performMouseDrag(this.targetMouseX, this.targetMouseY, 0, 0);
            NavigationPoint targetPoint = targetPointOptional.get();
            int screenX = (int) (targetPoint.getX() / ((double) resolution.getScaledWidth() / (double) mc.displayWidth));
            int screenY = (int) (targetPoint.getY() / ((double) resolution.getScaledHeight() / (double) mc.displayHeight));
            double lastTargetMouseX = this.targetMouseX;
            double lastTargetMouseY = this.targetMouseY;
            this.targetMouseX = this.prevTargetMouseX = screenX;
            this.targetMouseY = this.prevTargetMouseY = screenY;
            this.setMousePosition(screenX, screenY);
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_ITEM_PICKUP, 2.0F));
            //this.performMouseDrag(this.targetMouseX, this.targetMouseY, screenX - lastTargetMouseX, screenY - lastTargetMouseY);
        }
    }

    private List<NavigationPoint> gatherNavigationPoints(GuiScreen screen)
    {
        List<NavigationPoint> points = new ArrayList<>();

        if(screen instanceof GuiContainer)
        {
            GuiContainer containerScreen = (GuiContainer) screen;
            int guiLeft = containerScreen.getGuiLeft();
            int guiTop = containerScreen.getGuiTop();
            for(Slot slot : containerScreen.inventorySlots.inventorySlots)
            {
                if(containerScreen.getSlotUnderMouse() == slot)
                    continue;
                int posX = guiLeft + slot.xPos + 8;
                int posY = guiTop + slot.yPos + 8;
                points.add(new SlotNavigationPoint(posX, posY, slot));
            }
        }

        List<GuiButton> widgets = new ArrayList<>(screen.buttonList);
        if(screen instanceof IRecipeShownListener)
        {
            GuiRecipeBook recipeBook = ((IRecipeShownListener) screen).func_194310_f();
            if(recipeBook.isVisible())
            {
                widgets.add(((RecipeBookGuiMixin) recipeBook).getToggleRecipesBtn());
                widgets.addAll(((RecipeBookGuiMixin) recipeBook).getRecipeTabs());
                RecipeBookPage recipeBookPage = ((RecipeBookGuiMixin) recipeBook).getRecipeBookPage();
                widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).getButtons());
                widgets.add(((RecipeBookPageAccessor) recipeBookPage).getForwardButton());
                widgets.add(((RecipeBookPageAccessor) recipeBookPage).getBackButton());
            }
        }

        for(GuiButton widget : widgets)
        {
            if(widget == null || widget.isMouseOver() || !widget.visible)
                continue;
            int posX = widget.x + widget.width / 2;
            int posY = widget.y + widget.height / 2;
            points.add(new WidgetNavigationPoint(posX, posY, widget));
        }

        if(screen instanceof GuiContainerCreative)
        {
            int tabPage = CreativeScreenMixin.getTabPage();
            int start = tabPage * 10;
            int end = Math.min(CreativeTabs.CREATIVE_TAB_ARRAY.length, ((tabPage + 1) * 10 + 2));
            for(int i = start; i < end; i++)
            {
                CreativeTabs group = CreativeTabs.CREATIVE_TAB_ARRAY[i];
                if(group != null)
                {
                    points.add(this.getCreativeTabPoint((GuiContainerCreative) screen, group));
                }
            }
        }
        return points;
    }

    /**
     * Gets the navigation point of a creative tab.
     */
    private BasicNavigationPoint getCreativeTabPoint(GuiContainerCreative screen, CreativeTabs group)
    {
        boolean topRow = group.isOnTopRow();
        int column = group.getColumn();
        int width = 28;
        int height = 32;
        int x = screen.getGuiLeft() + width * column;
        int y = screen.getGuiTop();
        x = group.isAlignedRight() ? screen.getGuiLeft() + screen.getXSize() - width * (6 - column) : (column > 0 ? x + column : x);
        y = topRow ? y - width : y + (screen.getYSize() - 4);
        return new BasicNavigationPoint(x + width / 2.0, y + height / 2.0);
    }

    private void craftRecipeBookItem()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null)
            return;

        if(!(mc.currentScreen instanceof GuiContainer) || !(mc.currentScreen instanceof IRecipeShownListener))
            return;

        IRecipeShownListener listener = (IRecipeShownListener) mc.currentScreen;
        if(!listener.func_194310_f().isVisible())
            return;

        GuiContainer screen = (GuiContainer) mc.currentScreen;
        RecipeBookPage recipeBookPage = ((RecipeBookGuiMixin) listener.func_194310_f()).getRecipeBookPage();
        GuiButtonRecipe recipe = ((RecipeBookPageAccessor) recipeBookPage).getButtons().stream().filter(GuiButton::isMouseOver).findFirst().orElse(null);
        if(recipe != null)
        {
            Slot slot = screen.inventorySlots.inventorySlots.get(0);
            if(mc.player.inventory.getItemStack().isEmpty())
            {
                this.invokeMouseClick(screen, 0, screen.getGuiLeft() + slot.xPos + 8, screen.getGuiTop() + slot.yPos + 8);
            }
            else
            {
                this.invokeMouseReleased(screen, 0, screen.getGuiLeft() + slot.xPos + 8, screen.getGuiTop() + slot.yPos + 8);
            }
        }
    }

    private void moveMouseToClosestSlot(boolean moving, GuiScreen screen)
    {
        this.nearSlot = false;

        /* Makes the mouse attracted to slots. This helps with selecting items when using
         * a controller. */
        if(screen instanceof GuiContainer)
        {
            /* Prevents cursor from moving until at least some input is detected */
            if(!this.moved) return;

            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc);
            GuiContainer guiContainer = (GuiContainer) screen;
            int guiLeft = guiContainer.getGuiLeft();
            int guiTop = guiContainer.getGuiTop();
            int mouseX = (int) (this.targetMouseX * (double) resolution.getScaledWidth() / (double) mc.displayWidth);
            int mouseY = (int) (this.targetMouseY * (double) resolution.getScaledHeight() / (double) mc.displayHeight);

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
                this.nearSlot = true;
                int slotCenterXScaled = guiLeft + closestSlot.xPos + 8;
                int slotCenterYScaled = guiTop + closestSlot.yPos + 8;
                int slotCenterX = (int) (slotCenterXScaled / ((double) resolution.getScaledWidth() / (double) mc.displayWidth));
                int slotCenterY = (int) (slotCenterYScaled / ((double) resolution.getScaledHeight() / (double) mc.displayHeight));
                double deltaX = slotCenterX - targetMouseX;
                double deltaY = slotCenterY - targetMouseY;

                if(!moving)
                {
                    if(mouseX != slotCenterXScaled || mouseY != slotCenterYScaled)
                    {
                        this.targetMouseX += deltaX * 0.75;
                        this.targetMouseY += deltaY * 0.75;
                    }
                    else
                    {
                        this.mouseSpeedX = 0.0F;
                        this.mouseSpeedY = 0.0F;
                    }
                }

                this.mouseSpeedX *= 0.75F;
                this.mouseSpeedY *= 0.75F;
            }
            else
            {
                this.mouseSpeedX = 0.0F;
                this.mouseSpeedY = 0.0F;
            }
        }
        else
        {
            this.mouseSpeedX = 0.0F;
            this.mouseSpeedY = 0.0F;
        }
    }

    private void setMousePosition(double mouseX, double mouseY)
    {
        if(Controllable.getOptions().isVirtualMouse())
        {
            this.virtualMouseX = mouseX;
            this.virtualMouseY = mouseY;
        }
        else
        {
            Mouse.setCursorPosition((int) mouseX, (int) mouseY);
            this.preventReset = true;
        }
    }

    private void handleCreativeScrolling(GuiContainerCreative creative, Controller controller)
    {
        try
        {
            int i = (creative.inventorySlots.inventorySlots.size() + 9 - 1) / 9 - 5;
            int dir = 0;

            if(controller.getRThumbStickYValue() <= -0.8F)
            {
                dir = 1;
            }
            else if(controller.getRThumbStickYValue() >= 0.8F)
            {
                dir = -1;
            }

            Field field = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "field_147067_x");
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

    private void handleListScrolling(GuiSlot list, Controller controller)
    {
        double dir = 0;
        if(Math.abs(controller.getRThumbStickYValue()) >= 0.2F)
        {
            this.setControllerInUse();
            dir = controller.getRThumbStickYValue();
        }
        dir *= Minecraft.getMinecraft().getTickLength();
        list.scrollBy((int) (list.getAmountScrolled() + dir * 10));
    }

    private int getMouseX()
    {
        Minecraft mc = Minecraft.getMinecraft();
        double mouseX = Mouse.getX();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && this.lastUse > 0)
        {
            mouseX = this.virtualMouseX;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        return (int) (mouseX * (double) resolution.getScaledWidth() / (double) mc.displayWidth);
    }

    private int getMouseY()
    {
        Minecraft mc = Minecraft.getMinecraft();
        double mouseY = Mouse.getY();
        if(Controllable.getController() != null && Controllable.getOptions().isVirtualMouse() && this.lastUse > 0)
        {
            mouseY = this.virtualMouseY;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        return (int) (mouseY * (double) resolution.getScaledHeight() / (double) mc.displayHeight);
    }

    /**
     * Invokes a mouse click in a GUI. This is modified version that is designed for controllers.
     * Upon clicking, mouse released is called straight away to make sure dragging doesn't happen.
     *
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseClick(GuiScreen screen, int button)
    {
        if(screen != null)
        {
            int mouseX = this.getMouseX();
            int mouseY = this.getMouseY();
            this.invokeMouseClick(screen, button, mouseX, mouseY);
        }
    }

    private void invokeMouseClick(GuiScreen screen, int button, int mouseX, int mouseY)
    {
        if(screen != null)
        {
            screen.eventButton = button;
            screen.lastMouseEvent = Minecraft.getSystemTime();

            try
            {
                Method mouseClicked = ReflectionHelper.findMethod(GuiScreen.class, "mouseClicked", "func_73864_a", int.class, int.class, int.class);
                mouseClicked.setAccessible(true);
                mouseClicked.invoke(screen, mouseX, mouseY, button);
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
     * @param screen the screen instance
     * @param button the button to click with
     */
    private void invokeMouseReleased(GuiScreen screen, int button)
    {
        if(screen != null)
        {
            int mouseX = this.getMouseX();
            int mouseY = this.getMouseY();
            this.invokeMouseReleased(screen, button, mouseX, mouseY);
        }
    }

    private void invokeMouseReleased(GuiScreen screen, int button, int mouseX, int mouseY)
    {
        if(screen != null)
        {
            screen.eventButton = -1;

            try
            {
                Method mouseReleased = ReflectionHelper.findMethod(GuiScreen.class, "mouseReleased", "func_146286_b", int.class, int.class, int.class);
                mouseReleased.setAccessible(true);
                mouseReleased.invoke(screen, mouseX, mouseY, button);
            }
            catch(IllegalAccessException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
    }

    private enum Navigate
    {
        UP((p, x, y) -> p.getY() < y, (p, v) -> Math.abs(p.getX() - v.x)),
        DOWN((p, x, y) -> p.getY() > y + 1, (p, v) -> Math.abs(p.getX() - v.x)),
        LEFT((p, x, y) -> p.getX() < x, (p, v) -> Math.abs(p.getY() - v.y)),
        RIGHT((p, x, y) -> p.getX() > x + 1, (p, v) -> Math.abs(p.getY() - v.y));

        private NavigatePredicate predicate;
        private BiFunction<? super NavigationPoint, Vec3d, Double> keyExtractor;

        Navigate(NavigatePredicate predicate, BiFunction<? super NavigationPoint, Vec3d, Double> keyExtractor)
        {
            this.predicate = predicate;
            this.keyExtractor = keyExtractor;
        }

        public NavigatePredicate getPredicate()
        {
            return this.predicate;
        }

        public BiFunction<? super NavigationPoint, Vec3d, Double> getKeyExtractor()
        {
            return this.keyExtractor;
        }

        public Comparator<NavigationPoint> getMinComparator(int mouseX, int mouseY)
        {
            return Comparator.comparing(p -> this.keyExtractor.apply(p, new Vec3d(mouseX, mouseY, 0)));
        }

        public static void main(String[] args)
        {
            int slotX = 10;
            int slotY = 20;
            int mouseX = 50;
            int mouseY = 20;
            angle(new SlotNavigationPoint(slotX, slotY, null), mouseX, mouseY, 0);
        }

        private static boolean angle(NavigationPoint point, int mouseX, int mouseY, double offset)
        {
            double angle = Math.toDegrees(Math.atan2(point.getY() - mouseY, point.getX() - mouseX)) + offset;
            return angle > -45 && angle < 45;
        }
    }

    private interface NavigatePredicate
    {
        boolean test(NavigationPoint point, int mouseX, int mouseY);
    }
}
