package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.event.AvailableActionsEvent;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class RenderEvents
{
    public static final ResourceLocation CONTROLLER_BUTTONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons.png");

    private Map<Integer, Action> actions = new HashMap<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase == TickEvent.Phase.START && mc.player != null && !mc.gameSettings.hideGUI)
        {
            this.actions.clear();

            Map<ButtonBinding, Action> actionMap = new LinkedHashMap<>();

            ActionVisibility visibility = Controllable.getOptions().getActionVisibility();
            if(visibility == ActionVisibility.NONE)
                return;

            boolean verbose = visibility == ActionVisibility.ALL;

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
                            actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(I18n.format("controllable.action.pickup_stack"), Action.Side.LEFT));
                            actionMap.put(ButtonBindings.SPLIT_STACK, new Action(I18n.format("controllable.action.pickup_item"), Action.Side.LEFT));
                            actionMap.put(ButtonBindings.QUICK_MOVE, new Action(I18n.format("controllable.action.quick_move"), Action.Side.LEFT));
                        }
                    }
                }
                else
                {
                    actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(I18n.format("controllable.action.place_stack"), Action.Side.LEFT));
                    actionMap.put(ButtonBindings.SPLIT_STACK, new Action(I18n.format("controllable.action.place_item"), Action.Side.LEFT));
                }

                actionMap.put(ButtonBindings.INVENTORY, new Action(I18n.format("controllable.action.close_inventory"), Action.Side.RIGHT));
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
                        actionMap.put(ButtonBindings.ATTACK, new Action(I18n.format("controllable.action.break"), Action.Side.RIGHT));
                    }
                    else
                    {
                        actionMap.put(ButtonBindings.ATTACK, new Action(I18n.format("controllable.action.attack"), Action.Side.RIGHT));
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
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.eat"), Action.Side.RIGHT));
                            }
                            break;
                        case DRINK:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.drink"), Action.Side.RIGHT));
                            break;
                        case BLOCK:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.block"), Action.Side.RIGHT));
                            break;
                        case BOW:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.pull_bow"), Action.Side.RIGHT));
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
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.eat"), Action.Side.RIGHT));
                            }
                            break;
                        case DRINK:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.drink"), Action.Side.RIGHT));
                            break;
                        case BLOCK:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.block"), Action.Side.RIGHT));
                            break;
                        case BOW:
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.pull_bow"), Action.Side.RIGHT));
                            break;
                    }
                }
                else if(currentItem.getItem() instanceof ItemBlock)
                {
                    if(blockHit)
                    {
                        //TODO figure out logic to determine if block can be placed.
                        /*BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
                        BlockItem item = (BlockItem) currentItem.getItem();
                        ItemUseContext itemUseContext = new ItemUseContext(mc.player, Hand.MAIN_HAND, blockRayTraceResult);
                        BlockItemUseContext blockItemUseContext = new BlockItemUseContext(itemUseContext);
                        blockItemUseContext = item.getBlockItemUseContext(blockItemUseContext);
                        if(blockItemUseContext != null)
                        {
                            BlockState state = item.getStateForPlacement(blockItemUseContext);
                            if(state != null)
                            {
                                actions.put(Buttons.LEFT_TRIGGER, new Action(I18n.format("controllable.action.place_block"), Action.Side.RIGHT));
                            }
                        }*/
                        actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.place_block"), Action.Side.RIGHT));
                    }
                }
                else if(!currentItem.isEmpty() && !mc.player.isHandActive())
                {
                    actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.use_item"), Action.Side.RIGHT));
                }

                if(!mc.player.isSneaking() && blockHit && canOpenBlock && !mc.player.isHandActive())
                {
                    actionMap.put(ButtonBindings.USE_ITEM, new Action(I18n.format("controllable.action.interact"), Action.Side.RIGHT));
                }

                if(verbose)
                {
                    actionMap.put(ButtonBindings.JUMP, new Action(I18n.format("controllable.action.jump"), Action.Side.LEFT));
                }

                actionMap.put(ButtonBindings.INVENTORY, new Action(I18n.format("controllable.action.inventory"), Action.Side.LEFT));

                if(verbose && (!mc.player.getHeldItemOffhand().isEmpty() || !mc.player.inventory.getCurrentItem().isEmpty()))
                {
                    actionMap.put(ButtonBindings.SWAP_HANDS, new Action(I18n.format("controllable.action.swap_hands"), Action.Side.LEFT));
                }

                if(mc.player.isRiding())
                {
                    actionMap.put(ButtonBindings.SNEAK, new Action(I18n.format("controllable.action.dismount"), Action.Side.RIGHT));
                }
                else if(verbose)
                {
                    actionMap.put(ButtonBindings.SNEAK, new Action(I18n.format("controllable.action.sneak"), Action.Side.RIGHT));
                }

                if(!mc.player.inventory.getCurrentItem().isEmpty())
                {
                    actionMap.put(ButtonBindings.DROP_ITEM, new Action(I18n.format("controllable.action.drop_item"), Action.Side.LEFT));
                }
            }

            MinecraftForge.EVENT_BUS.post(new AvailableActionsEvent(this.actions));
            MinecraftForge.EVENT_BUS.post(new GatherActionsEvent(actionMap, visibility));
            actionMap.forEach((binding, action) -> this.actions.put(binding.getButton(), action));
        }
    }

    @SubscribeEvent
    public void onRenderScreen(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(Controllable.getInput().getLastUse() <= 0)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.gameSettings.hideGUI)
            return;

        if(Controllable.getController() == null)
            return;

        GlStateManager.pushMatrix();
        {
            if(!MinecraftForge.EVENT_BUS.post(new RenderAvailableActionsEvent()))
            {
                GuiIngame guiIngame = mc.ingameGUI;
                boolean isChatVisible = mc.currentScreen == null && guiIngame.getChatGUI().drawnChatLines.stream().anyMatch(chatLine -> guiIngame.getUpdateCounter() - chatLine.getUpdatedCounter() < 200);

                int leftIndex = 0;
                int rightIndex = 0;
                for(int button : this.actions.keySet())
                {
                    Action action = this.actions.get(button);
                    Action.Side side = action.getSide();

                    int texU = button * 13;
                    int texV = Controllable.getOptions().getControllerType().ordinal() * 13;
                    int size = 13;

                    ScaledResolution resolution = new ScaledResolution(mc);
                    int x = side == Action.Side.LEFT ? 5 : resolution.getScaledWidth() - 5 - size;
                    int y = resolution.getScaledHeight() + (side == Action.Side.LEFT ? leftIndex : rightIndex) * -15 - size - 5;

                    mc.getTextureManager().bindTexture(CONTROLLER_BUTTONS);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableLighting();

                    if(isChatVisible && side == Action.Side.LEFT && leftIndex >= 2)
                        continue;

                    /* Draw buttons icon */
                    Gui.drawModalRectWithCustomSizedTexture(x, y, texU, texV, size, size, 256, 256);

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
