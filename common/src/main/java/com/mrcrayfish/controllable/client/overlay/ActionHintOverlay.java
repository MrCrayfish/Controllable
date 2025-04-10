package com.mrcrayfish.controllable.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionDescriptions;
import com.mrcrayfish.controllable.client.settings.ActionVisibility;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.settings.ButtonIcons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ActionHintOverlay implements IOverlay
{
    private static final int BUTTON_SIZE = 13;

    private final Map<Integer, Action> actions = new HashMap<>();

    @Override
    public boolean isVisible()
    {
        Controller controller = Controllable.getController();
        return !Minecraft.getInstance().options.hideGui && controller != null && (!Config.CLIENT.options.overlayTimeout.get() || controller.isBeingUsed());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, DeltaTracker tracker)
    {
        if(this.actions.isEmpty())
            return;

        if(Config.CLIENT.options.consoleHotbar.get())
        {
            this.drawConsoleHints(graphics);
        }
        else
        {
            this.drawSidedHints(graphics);
        }
    }

    private void drawConsoleHints(GuiGraphics graphics)
    {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 400);

        Minecraft mc = Minecraft.getInstance();
        pose.translate(5, mc.getWindow().getGuiScaledHeight() - BUTTON_SIZE - 5, 0);

        List<Pair<Integer, Action>> sortedActions = this.actions.entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
            .sorted(Map.Entry.comparingByValue())
            .toList();
        for(Pair<Integer, Action> pair : sortedActions)
        {
            int button = pair.getKey();
            Action action = pair.getValue();

            // Draw button
            int buttonU = button * BUTTON_SIZE;
            int buttonV = Config.CLIENT.options.controllerIcons.get().ordinal() * BUTTON_SIZE;
            graphics.blit(ButtonIcons.TEXTURE, 0, 0, BUTTON_SIZE, BUTTON_SIZE, buttonU, buttonV, BUTTON_SIZE, BUTTON_SIZE, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);

            // Draw text and background
            int textWidth = mc.font.width(action.getDescription());
            this.drawHintBackground(graphics, BUTTON_SIZE + 5, 0, textWidth, BUTTON_SIZE);
            this.drawHintLabel(graphics, action.getDescription(), BUTTON_SIZE + 5, 3);

            // Finally translate for next action to be positioned correctly
            pose.translate(BUTTON_SIZE + 5 + textWidth + 10, 0, 0);
        }

        pose.popPose();
    }

    private void drawSidedHints(GuiGraphics graphics)
    {
        int[] positions = new int[2];
        for(int button : this.actions.keySet())
        {
            Action action = this.actions.get(button);
            Action.Side side = ClientHelper.isSubtitleShowing() ? Action.Side.LEFT : action.getSide();

            if(ClientHelper.isChatVisible() && side == Action.Side.LEFT && positions[0] >= 2)
                continue;

            int position = positions[side.ordinal()];
            this.drawSideHint(graphics, action, side, button, position);
            positions[side.ordinal()] = position + 1;
        }
    }

    private void drawSideHint(GuiGraphics graphics, Action action, Action.Side side, int button, int position)
    {
        Minecraft mc = Minecraft.getInstance();

        // Draw button icon
        int texU = button * BUTTON_SIZE;
        int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * BUTTON_SIZE;
        int x = side == Action.Side.LEFT ? 5 : mc.getWindow().getGuiScaledWidth() - 5 - BUTTON_SIZE;
        int y = mc.getWindow().getGuiScaledHeight() + position * -15 - BUTTON_SIZE - 5;
        graphics.blit(ButtonIcons.TEXTURE, x, y, texU, texV, BUTTON_SIZE, BUTTON_SIZE, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);

        // Draw label
        int textWidth = mc.font.width(action.getDescription());
        int labelX = side == Action.Side.LEFT ? x + 18 : x - 5 - textWidth;
        this.drawHintBackground(graphics, labelX, y, textWidth, 13);
        this.drawHintLabel(graphics, action.getDescription(), labelX, y + 3);
    }

    private void drawHintBackground(GuiGraphics graphics, int x, int y, int width, int height)
    {
        if(Config.CLIENT.options.drawHintBackground.get())
        {
            Minecraft mc = Minecraft.getInstance();
            int backgroundColor = mc.options.getBackgroundColor(0.5F);
            ScreenHelper.drawRoundedBox(graphics, x, y, width, height, backgroundColor);
        }
    }

    private void drawHintLabel(GuiGraphics graphics, Component label, int x, int y)
    {
        Minecraft mc = Minecraft.getInstance();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400F);
        graphics.drawString(mc.font, label, x, y, 0xFFFFFFFF);
        graphics.pose().popPose();
    }

    @Override
    public void tick()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.options.hideGui)
            return;

        this.actions.clear();

        ActionVisibility visibility = Config.CLIENT.options.showButtonHints.get();
        if(visibility == ActionVisibility.NONE)
            return;

        boolean verbose = visibility == ActionVisibility.ALL;
        Map<ButtonBinding, Action> actionMap = new LinkedHashMap<>();
        if(mc.screen instanceof AbstractContainerScreen<?> containerScreen)
        {
            if(mc.player.inventoryMenu.getCarried().isEmpty())
            {
                Slot slot = ClientServices.CLIENT.getSlotUnderMouse(containerScreen);
                if(slot != null && slot.hasItem())
                {
                    actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(ActionDescriptions.PICKUP_STACK, Action.Side.LEFT));
                    actionMap.put(ButtonBindings.SPLIT_STACK, new Action(ActionDescriptions.SPLIT_STACK, Action.Side.LEFT));
                    actionMap.put(ButtonBindings.QUICK_MOVE, new Action(ActionDescriptions.QUICK_MOVE, Action.Side.LEFT));
                }
            }
            else
            {
                actionMap.put(ButtonBindings.PICKUP_ITEM, new Action(ActionDescriptions.PLACE_STACK, Action.Side.LEFT));
                actionMap.put(ButtonBindings.SPLIT_STACK, new Action(ActionDescriptions.PLACE_ITEM, Action.Side.LEFT));

                Slot slot = ClientServices.CLIENT.getSlotUnderMouse(containerScreen);
                if(slot != null && slot.hasItem())
                {
                    actionMap.put(ButtonBindings.QUICK_MOVE, new Action(ActionDescriptions.QUICK_MOVE, Action.Side.LEFT));
                }
            }

            actionMap.put(ButtonBindings.CLOSE_INVENTORY, new Action(ActionDescriptions.CLOSE_INVENTORY, Action.Side.RIGHT));
        }
        else if(mc.screen == null)
        {
            if(Controllable.getRadialMenu().isVisible())
            {
                if(Controllable.getRadialMenu().getSelected() != null)
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
                    canOpenBlock = mc.level != null && mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BaseEntityBlock;
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

                ItemStack currentItem = mc.player.getMainHandItem();
                if(currentItem.getUseAnimation() != UseAnim.NONE)
                {
                    switch(currentItem.getUseAnimation())
                    {
                        case EAT:
                            FoodProperties properties = currentItem.get(DataComponents.FOOD);
                            if(properties != null && mc.player.canEat(properties.canAlwaysEat()))
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
        actionMap.forEach((binding, action) -> {
            if(!binding.isUnbound()) {
                this.actions.put(binding.getButton(), action);
            }
        });
    }
}
