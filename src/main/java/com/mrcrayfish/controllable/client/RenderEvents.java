package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.event.AvailableActionsEvent;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
import com.mrcrayfish.controllable.event.RenderAvailableActionsEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

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
    private int selectedRadialIndex = -1;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.START && mc.player != null && !mc.gameSettings.hideGUI)
        {
            this.actions.clear();

            Map<ButtonBinding, Action> actionMap = new LinkedHashMap<>();

            ActionVisibility visibility = Config.CLIENT.options.showActions.get();
            if(visibility == ActionVisibility.NONE)
                return;

            boolean verbose = visibility == ActionVisibility.ALL;

            if(mc.currentScreen instanceof ContainerScreen)
            {
                if(mc.player.inventory.getItemStack().isEmpty())
                {
                    ContainerScreen container = (ContainerScreen) mc.currentScreen;
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
                boolean blockHit = mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK;
                boolean canOpenBlock = false;
                if(blockHit)
                {
                    BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
                    canOpenBlock = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock() instanceof ContainerBlock;
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
                if(offHandStack.getUseAction() != UseAction.NONE)
                {
                    switch(offHandStack.getUseAction())
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
                if(currentItem.getUseAction() != UseAction.NONE)
                {
                    switch(currentItem.getUseAction())
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

                if(mc.player.isPassenger())
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

        Minecraft mc = Minecraft.getInstance();
        if(mc.gameSettings.hideGUI)
            return;

        if(Controllable.getController() != null)
        {
            if(Controllable.getInput().getLastUse() <= 0)
            {
                return;
            }

            this.renderHints();
            this.renderMiniPlayer();
            this.renderRadialMenu();
        }
        else if(mc.currentScreen == null)
        {
            RenderSystem.disableDepthTest();
            int width = mc.getMainWindow().getScaledWidth();
            int height = mc.getMainWindow().getScaledHeight();
            AbstractGui.fill(new MatrixStack(), 0, 0, width, height, -1072689136);
            AbstractGui.drawCenteredString(new MatrixStack(), mc.fontRenderer, new TranslationTextComponent("controllable.gui.controller_only").mergeStyle(TextFormatting.BOLD).mergeStyle(TextFormatting.YELLOW), width / 2, height / 2 - 15, 0xFFFFFFFF);
            AbstractGui.drawCenteredString(new MatrixStack(), mc.fontRenderer, new TranslationTextComponent("controllable.gui.plug_in_controller"), width / 2, height / 2, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    private void renderHints()
    {
        if(!MinecraftForge.EVENT_BUS.post(new RenderAvailableActionsEvent()))
        {
            Minecraft mc = Minecraft.getInstance();
            IngameGui guiIngame = mc.ingameGUI;
            boolean isChatVisible = mc.currentScreen == null && guiIngame.getChatGUI().drawnChatLines.stream().anyMatch(chatLine -> guiIngame.getTicks() - chatLine.getUpdatedCounter() < 200);

            int leftIndex = 0;
            int rightIndex = 0;
            for(int button : this.actions.keySet())
            {
                Action action = this.actions.get(button);
                Action.Side side = action.getSide();

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

                int x = side == Action.Side.LEFT ? 5 : mc.getMainWindow().getScaledWidth() - 5 - size;
                int y = mc.getMainWindow().getScaledHeight() + (side == Action.Side.LEFT ? leftIndex : rightIndex) * -15 - size - 5;

                mc.getTextureManager().bindTexture(CONTROLLER_BUTTONS);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableLighting();

                if(isChatVisible && side == Action.Side.LEFT && leftIndex >= 2) continue;

                /* Draw buttons icon */
                MatrixStack matrixStack = new MatrixStack();
                Widget.blit(matrixStack, x, y, texU, texV, size, size, 256, 256);

                /* Draw description text */
                if(side == Action.Side.LEFT)
                {
                    mc.fontRenderer.drawString(matrixStack, action.getDescription(), x + 18, y + 3, Color.WHITE.getRGB());
                    leftIndex++;
                }
                else
                {
                    int width = mc.fontRenderer.getStringWidth(action.getDescription());
                    mc.fontRenderer.drawString(matrixStack, action.getDescription(), x - 5 - width, y + 3, Color.WHITE.getRGB());
                    rightIndex++;
                }
            }
        }
    }

    private void renderMiniPlayer()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.currentScreen == null && Config.CLIENT.options.renderMiniPlayer.get())
        {
            if(!MinecraftForge.EVENT_BUS.post(new RenderPlayerPreviewEvent()))
            {
                InventoryScreen.drawEntityOnScreen(20, 45, 20, 0, 0, mc.player);
            }
        }
    }

    private void renderRadialMenu()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        double selectedAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(controller.getRThumbStickYValue(), controller.getRThumbStickXValue())) - 90) + 180;
        boolean canSelect = Math.abs(controller.getRThumbStickYValue()) > 0.5F || Math.abs(controller.getRThumbStickXValue()) > 0.5F;
        int segments = 8; // Segments will be based on bound actions
        double segmentSize = 360.0 / segments;
        float innerRadius = 60F;
        float outerRadius = 100F;

        int radialIndex = canSelect ? (int) (selectedAngle / segmentSize) : -1;
        if(radialIndex != this.selectedRadialIndex)
        {
            this.setSelectedRadialIndex(radialIndex);
        }

        MatrixStack matrixStack = new MatrixStack();
        Minecraft mc = Minecraft.getInstance();
        matrixStack.translate(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2F, 0);

        int wheelColor = 0x88000000;
        float wheelAlpha = (wheelColor >> 24 & 255) / 255F;
        float wheelRed = (wheelColor >> 16 & 255) / 255F;
        float wheelGreen = (wheelColor >> 8 & 255) / 255F;
        float wheelBlue = (wheelColor & 255) / 255F;

        int highlightColor = 0xFFFFFF;
        float highlightRed = (highlightColor >> 16 & 255) / 255F;
        float highlightGreen = (highlightColor >> 8 & 255) / 255F;
        float highlightBlue = (highlightColor & 255) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            boolean selected = this.selectedRadialIndex == i - 1;
            //float newAlpha = selected ? Math.min(1.0F, wheelAlpha + 0.3F) : wheelAlpha;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            if(!selected)
            {
                buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(wheelRed, wheelGreen, wheelBlue, wheelAlpha).endVertex();
                buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(wheelRed, wheelGreen, wheelBlue, wheelAlpha).endVertex();
            }
            else
            {
                buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(highlightRed, highlightGreen, highlightBlue, wheelAlpha).endVertex();
                buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(highlightRed, highlightGreen, highlightBlue, wheelAlpha).endVertex();
            }
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        RenderSystem.lineWidth(4F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(1.0F, 1.0F, 1.0F, 0.75F).endVertex();
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        RenderSystem.lineWidth(4F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(1.0F, 1.0F, 1.0F, 0.75F).endVertex();
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void setSelectedRadialIndex(int index)
    {
        this.selectedRadialIndex = index;
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, 1.5F));
    }
}
