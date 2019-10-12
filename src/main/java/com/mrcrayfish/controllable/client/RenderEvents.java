package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.event.AvailableActionsEvent;
import com.mrcrayfish.controllable.event.RenderAvailableActionsEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class RenderEvents
{
    private static final ResourceLocation CONTROLLER_BUTTONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons.png");

    private Map<Integer, Action> actions = new HashMap<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase == TickEvent.Phase.START && mc.player != null && !mc.gameSettings.hideGUI)
        {
            actions = new HashMap<>();

            if(mc.currentScreen instanceof GuiContainer)
            {
                if(mc.player.inventory.getItemStack().isEmpty())
                {
                    GuiContainer container = (GuiContainer) mc.currentScreen;
                    if(container.getSlotUnderMouse() != null)
                    {
                        Slot slot = container.getSlotUnderMouse();
                        if(slot.getHasStack())
                        {
                            actions.put(Buttons.A, new Action(I18n.format("controllable.action.pickup_stack"), Action.Side.LEFT));
                            actions.put(Buttons.X, new Action(I18n.format("controllable.action.pickup_item"), Action.Side.LEFT));
                            actions.put(Buttons.B, new Action(I18n.format("controllable.action.quick_move"), Action.Side.LEFT));
                        }
                    }
                }
                else
                {
                    actions.put(Buttons.A, new Action(I18n.format("controllable.action.place_stack"), Action.Side.LEFT));
                    actions.put(Buttons.X, new Action(I18n.format("controllable.action.place_item"), Action.Side.LEFT));
                }

                actions.put(Buttons.Y, new Action(I18n.format("controllable.action.close_inventory"), Action.Side.RIGHT));
            }
            else if(mc.currentScreen == null)
            {
                boolean blockHit = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
                boolean canOpenBlock = false;
                if(blockHit)
                {
                    canOpenBlock = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() instanceof BlockContainer;
                }

                if(!mc.player.isHandActive())
                {
                    if(blockHit)
                    {
                        actions.put(Buttons.RIGHT_TRIGGER, new Action(I18n.format("controllable.action.break"), Action.Side.RIGHT));
                    }
                    else
                    {
                        actions.put(Buttons.RIGHT_TRIGGER, new Action(I18n.format("controllable.action.attack"), Action.Side.RIGHT));
                    }
                }

                ItemStack offHandStack = mc.player.getHeldItemOffhand();
                if(offHandStack.getItemUseAction() != EnumAction.NONE)
                {
                    switch(offHandStack.getItemUseAction())
                    {
                        case EAT:
                            if(mc.player.getFoodStats().needFood())
                            {
                                actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.eat"), Action.Side.RIGHT));
                            }
                            break;
                        case DRINK:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.drink"), Action.Side.RIGHT));
                            break;
                        case BLOCK:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.block"), Action.Side.RIGHT));
                            break;
                        case BOW:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.pull_bow"), Action.Side.RIGHT));
                            break;
                    }
                }

                ItemStack currentItem = mc.player.inventory.getCurrentItem();
                if(currentItem.getItemUseAction() != EnumAction.NONE)
                {
                    switch(currentItem.getItemUseAction())
                    {
                        case EAT:
                            if(mc.player.getFoodStats().needFood())
                            {
                                actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.eat"), Action.Side.RIGHT));
                            }
                            break;
                        case DRINK:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.drink"), Action.Side.RIGHT));
                            break;
                        case BLOCK:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.block"), Action.Side.RIGHT));
                            break;
                        case BOW:
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.pull_bow"), Action.Side.RIGHT));
                            break;
                    }
                }
                else if(currentItem.getItem() instanceof ItemBlock)
                {
                    if(blockHit)
                    {
                        ItemBlock block = (ItemBlock) currentItem.getItem();
                        if(block.getBlock().canPlaceBlockAt(mc.world, mc.objectMouseOver.getBlockPos().offset(mc.objectMouseOver.sideHit)))
                        {
                            actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.place_block"), Action.Side.RIGHT));
                        }
                    }
                }
                else if(!currentItem.isEmpty() && !mc.player.isHandActive())
                {
                    actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.use_item"), Action.Side.RIGHT));
                }

                if(!mc.player.isSneaking() && blockHit && canOpenBlock && !mc.player.isHandActive())
                {
                    actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.interact"), Action.Side.RIGHT));
                }

                //actions.put(Buttons.A, new Action(I18n.format("controllable.action.jump"), Action.Side.LEFT)); //TODO make a verbose action config option

                actions.put(Buttons.Y, new Action(I18n.format("controllable.action.inventory"), Action.Side.LEFT));

                if(!mc.player.getHeldItemOffhand().isEmpty() || !mc.player.inventory.getCurrentItem().isEmpty())
                {
                    //actions.put(Buttons.X, new Action(I18n.format("controllable.action.swap_hands"), Action.Side.LEFT));  //TODO make a verbose action config option
                }

                if(mc.player.isRiding())
                {
                    actions.put(Buttons.LEFT_THUMB_STICK, new Action(I18n.format("controllable.action.dismount"), Action.Side.RIGHT));
                }
                else
                {
                    //actions.put(Buttons.LEFT_THUMB_STICK, new Action(I18n.format("controllable.action.sneak"), Action.Side.RIGHT));  //TODO make a verbose action config option
                }

                if(!mc.player.inventory.getCurrentItem().isEmpty())
                {
                    actions.put(Buttons.DPAD_DOWN, new Action(I18n.format("controllable.action.drop_item"), Action.Side.LEFT));
                }
            }

            MinecraftForge.EVENT_BUS.post(new AvailableActionsEvent(actions));
        }
    }

    @SubscribeEvent
    public void onRenderScreen(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(ControllerInput.lastUse <= 0)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.gameSettings.hideGUI)
            return;

        GlStateManager.pushMatrix();
        {
            if(!MinecraftForge.EVENT_BUS.post(new RenderAvailableActionsEvent()))
            {
                GuiIngame guiIngame = mc.ingameGUI;
                boolean isChatVisible = mc.currentScreen == null && guiIngame.getChatGUI().drawnChatLines.stream().anyMatch(chatLine -> guiIngame.getUpdateCounter() - chatLine.getUpdatedCounter() < 200);

                int leftIndex = 0;
                int rightIndex = 0;
                for(Integer button : actions.keySet())
                {
                    Action action = actions.get(button);
                    Action.Side side = action.getSide();

                    float texU = (button % 19) * 13F;
                    float texV = (button / 19) * 13F;
                    int size = 13;

                    ScaledResolution resolution = new ScaledResolution(mc);
                    int x = side == Action.Side.LEFT ? 5 : resolution.getScaledWidth() - 5 - size;
                    int y = resolution.getScaledHeight() + (side == Action.Side.LEFT ? leftIndex : rightIndex) * -15 - size - 5;

                    mc.getTextureManager().bindTexture(CONTROLLER_BUTTONS);
                    GlStateManager.color(1.0F, 1.0F, 1.0F);
                    GlStateManager.disableLighting();

                    if(isChatVisible && side == Action.Side.LEFT && leftIndex >= 2)
                        continue;

                    /* Draw buttons icon */
                    Gui.drawScaledCustomSizeModalRect(x, y, texU, texV, size, size, size, size, 256, 256);

                    /* Draw description text */
                    if(side == Action.Side.LEFT)
                    {
                        mc.fontRenderer.drawString(action.getDescription(), x + 18, y + 3, Color.WHITE.getRGB());
                        leftIndex++;
                    }
                    else
                    {
                        int width = mc.fontRenderer.getStringWidth(action.getDescription());
                        mc.fontRenderer.drawString(action.getDescription(), x - 5 - width, y + 3, Color.WHITE.getRGB());
                        rightIndex++;
                    }
                }
            }

            if(mc.player != null && mc.currentScreen == null && Controllable.getOptions().isRenderMiniPlayer())
            {
                if(!MinecraftForge.EVENT_BUS.post(new RenderPlayerPreviewEvent()))
                {
                    GuiInventory.drawEntityOnScreen(20, 45, 20, 0, 0, mc.player);
                }
            }
        }
        GlStateManager.popMatrix();
    }
}
