package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
import com.mrcrayfish.controllable.event.RenderAvailableActionsEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    private int selectedRadialIndex = -1;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.START && mc.player != null && !mc.options.hideGui)
        {
            this.actions.clear();

            Map<ButtonBinding, Action> actionMap = new LinkedHashMap<>();

            ActionVisibility visibility = Config.CLIENT.options.showActions.get();
            if(visibility == ActionVisibility.NONE)
                return;

            boolean verbose = visibility == ActionVisibility.ALL;

            if(mc.screen instanceof AbstractContainerScreen<?> containerScreen)
            {
                if(mc.player.inventoryMenu.getCarried().isEmpty())
                {
                    if(containerScreen.getSlotUnderMouse() != null)
                    {
                        Slot slot = containerScreen.getSlotUnderMouse();
                        if(slot.hasItem())
                        {
                            actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(ActionDescriptions.PICKUP_STACK, Action.Side.LEFT));
                            actionMap.put(ButtonBindings.SPLIT_STACK, new Action(ActionDescriptions.SPLIT_STACK, Action.Side.LEFT));
                            actionMap.put(ButtonBindings.QUICK_MOVE, new Action(ActionDescriptions.QUICK_MOVE, Action.Side.LEFT));
                        }
                    }
                }
                else
                {
                    actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(ActionDescriptions.PLACE_STACK, Action.Side.LEFT));
                    actionMap.put(ButtonBindings.SPLIT_STACK, new Action(ActionDescriptions.PLACE_ITEM, Action.Side.LEFT));

                    // You can still quick move items if holding one with cursor
                    if(containerScreen.getSlotUnderMouse() != null)
                    {
                        Slot slot = containerScreen.getSlotUnderMouse();
                        if(slot.hasItem())
                        {
                            actionMap.put(ButtonBindings.QUICK_MOVE, new Action(ActionDescriptions.QUICK_MOVE, Action.Side.LEFT));
                        }
                    }
                }

                actionMap.put(ButtonBindings.CLOSE_INVENTORY, new Action(ActionDescriptions.CLOSE_INVENTORY, Action.Side.RIGHT));
            }
            else if(mc.screen == null)
            {
                if(RadialMenuHandler.instance().isVisible())
                {
                    if(RadialMenuHandler.instance().getSelected() != null)
                    {
                        actionMap.put(ButtonBindings.RADIAL_MENU, new Action(ActionDescriptions.PERFORM_ACTION, Action.Side.RIGHT));
                    }
                    else
                    {
                        actionMap.put(ButtonBindings.RADIAL_MENU, new Action(ActionDescriptions.CLOSE_MENU, Action.Side.RIGHT));
                    }
                }
                else
                {
                    boolean blockHit = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK;
                    boolean canOpenBlock = false;
                    if(blockHit)
                    {
                        BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
                        canOpenBlock = mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BaseEntityBlock;
                    }

                    if(!mc.player.isUsingItem())
                    {
                        if(blockHit)
                        {
                            actionMap.put(ButtonBindings.ATTACK, new Action(ActionDescriptions.BREAK, Action.Side.RIGHT));
                        }
                        else
                        {
                            actionMap.put(ButtonBindings.ATTACK, new Action(ActionDescriptions.ATTACK, Action.Side.RIGHT));
                        }
                    }

                    ItemStack offHandStack = mc.player.getOffhandItem();
                    if(offHandStack.getUseAnimation() != UseAnim.NONE)
                    {
                        switch(offHandStack.getUseAnimation())
                        {
                            case EAT:
                                if(mc.player.getFoodData().needsFood())
                                {
                                    actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.EAT, Action.Side.RIGHT));
                                }
                                break;
                            case DRINK:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.DRINK, Action.Side.RIGHT));
                                break;
                            case BLOCK:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.BLOCK, Action.Side.RIGHT));
                                break;
                            case BOW:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.PULL_BOW, Action.Side.RIGHT));
                                break;
                        }
                    }

                    ItemStack currentItem = mc.player.containerMenu.getCarried(); //TODO test
                    if(currentItem.getUseAnimation() != UseAnim.NONE)
                    {
                        switch(currentItem.getUseAnimation())
                        {
                            case EAT:
                                if(mc.player.getFoodData().needsFood())
                                {
                                    actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.EAT, Action.Side.RIGHT));
                                }
                                break;
                            case DRINK:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.DRINK, Action.Side.RIGHT));
                                break;
                            case BLOCK:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.BLOCK, Action.Side.RIGHT));
                                break;
                            case BOW:
                                actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.PULL_BOW, Action.Side.RIGHT));
                                break;
                        }
                    }
                    else if(currentItem.getItem() instanceof BlockItem)
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
                            actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.PLACE_BLOCK, Action.Side.RIGHT));
                        }
                    }
                    else if(!currentItem.isEmpty() && !mc.player.isUsingItem())
                    {
                        actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.USE_ITEM, Action.Side.RIGHT));
                    }

                    if(!mc.player.isCrouching() && blockHit && canOpenBlock && !mc.player.isUsingItem())
                    {
                        actionMap.put(ButtonBindings.USE_ITEM, new Action(ActionDescriptions.INTERACT, Action.Side.RIGHT));
                    }

                    if(verbose)
                    {
                        actionMap.put(ButtonBindings.JUMP, new Action(ActionDescriptions.JUMP, Action.Side.LEFT));
                    }

                    actionMap.put(ButtonBindings.OPEN_INVENTORY, new Action(ActionDescriptions.OPEN_INVENTORY, Action.Side.LEFT));

                    if(verbose && (!mc.player.getOffhandItem().isEmpty() || !mc.player.containerMenu.getCarried().isEmpty())) //TODO test
                    {
                        actionMap.put(ButtonBindings.SWAP_HANDS, new Action(ActionDescriptions.SWAP_HANDS, Action.Side.LEFT));
                    }

                    if(mc.player.isPassenger())
                    {
                        actionMap.put(ButtonBindings.SNEAK, new Action(ActionDescriptions.DISMOUNT, Action.Side.RIGHT));
                    }
                    else if(verbose)
                    {
                        actionMap.put(ButtonBindings.SNEAK, new Action(ActionDescriptions.SNEAK, Action.Side.RIGHT));
                    }

                    if(!mc.player.inventoryMenu.getCarried().isEmpty()) //TODO test
                    {
                        actionMap.put(ButtonBindings.DROP_ITEM, new Action(ActionDescriptions.DROP_ITEM, Action.Side.LEFT));
                    }
                }
            }

            MinecraftForge.EVENT_BUS.post(new GatherActionsEvent(actionMap, visibility));
            actionMap.forEach((binding, action) -> this.actions.put(binding.getButton(), action));
        }
    }

    @SubscribeEvent
    public void onRenderScreen(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.options.hideGui)
            return;

        if(Controllable.getController() != null)
        {
            if(Controllable.getInput().getLastUse() > 0)
            {
                this.renderHints();
                this.renderMiniPlayer();
            }
        }
        else if(mc.screen == null && Config.SERVER.restrictToController.get())
        {
            RenderSystem.disableDepthTest();
            int width = mc.getWindow().getScreenWidth();
            int height = mc.getWindow().getScreenHeight(); //TODO test
            Screen.fill(new PoseStack(), 0, 0, width, height, -1072689136);
            Screen.drawCenteredString(new PoseStack(), mc.font, Component.translatable("controllable.gui.controller_only").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW), width / 2, height / 2 - 15, 0xFFFFFFFF);
            Screen.drawCenteredString(new PoseStack(), mc.font, Component.translatable("controllable.gui.plug_in_controller"), width / 2, height / 2, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    private void renderHints()
    {
        if(!MinecraftForge.EVENT_BUS.post(new RenderAvailableActionsEvent()))
        {
            Minecraft mc = Minecraft.getInstance();
            Gui guiIngame = mc.gui;
            boolean isChatVisible = mc.screen == null && guiIngame.getChat().trimmedMessages.stream().anyMatch(chatLine -> guiIngame.getGuiTicks() - chatLine.addedTime() < 200);

            int leftIndex = 0;
            int rightIndex = 0;
            for(int button : this.actions.keySet())
            {
                Action action = this.actions.get(button);
                Action.Side side = action.getSide();
                
                if(mc.options.showSubtitles().get() && mc.screen == null)
                {
                    side = Action.Side.LEFT;
                }
/*
                int remappedButton = button;
                Controller controller = Controllable.getController();
                Mappings.Entry mapping = controller.getMapping();
                if(mapping != null)
                {
                    remappedButton = mapping.remap(button);
                }
*/

                int texU = button * 13;
                int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * 13;
                int size = 13;

                int x = side == Action.Side.LEFT ? 5 : mc.getWindow().getGuiScaledWidth() - 5 - size; //TODO test
                int y = mc.getWindow().getGuiScaledHeight() + (side == Action.Side.LEFT ? leftIndex : rightIndex) * -15 - size - 5;

                RenderSystem.setShaderTexture(0, CONTROLLER_BUTTONS);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                if(isChatVisible && side == Action.Side.LEFT && leftIndex >= 2)
                    continue;

                /* Draw buttons icon */
                PoseStack poseStack = new PoseStack();
                Screen.blit(poseStack, x, y, texU, texV, size, size, 256, 256);

                /* Draw description text */
                if(side == Action.Side.LEFT)
                {
                    int textWidth = mc.font.width(action.getDescription());
                    this.drawHintBackground(poseStack, x + 18, y, textWidth, 13);
                    mc.font.draw(poseStack, action.getDescription(), x + 18, y + 3, 0xFFFFFFFF);
                    leftIndex++;
                }
                else
                {
                    int textWidth = mc.font.width(action.getDescription());
                    this.drawHintBackground(poseStack, x - 5 - textWidth, y, textWidth, 13);
                    mc.font.draw(poseStack, action.getDescription(), x - 5 - textWidth, y + 3, 0xFFFFFFFF);
                    rightIndex++;
                }
            }
        }
    }

    private void drawHintBackground(PoseStack poseStack, int x, int y, int width, int height)
    {
        if(!Config.CLIENT.options.hintBackground.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        int backgroundColor = mc.options.getBackgroundColor(0.5F);
        Screen.fill(poseStack, x - 3 + 1, y, x + width + 2 - 1, y + 1, backgroundColor);
        Screen.fill(poseStack, x - 3, y + 1, x + width + 2, y + height - 1, backgroundColor);
        Screen.fill(poseStack, x - 3 + 1, y + height - 1, x + width + 2 - 1, y + height, backgroundColor);
    }

    private void renderMiniPlayer()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.screen == null && Config.CLIENT.options.renderMiniPlayer.get())
        {
            if(!MinecraftForge.EVENT_BUS.post(new RenderPlayerPreviewEvent()))
            {
                InventoryScreen.renderEntityInInventory(20, 45, 20, 0, 0, mc.player);
            }
        }
    }
}
