package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    private static final Set<ResourceLocation> OFFSET_LAYERS = Util.make(new HashSet<>(), set -> {
        set.add(VanillaGuiLayers.HOTBAR);
        set.add(VanillaGuiLayers.JUMP_METER);
        set.add(VanillaGuiLayers.EXPERIENCE_BAR);
        set.add(VanillaGuiLayers.PLAYER_HEALTH);
        set.add(VanillaGuiLayers.ARMOR_LEVEL);
        set.add(VanillaGuiLayers.FOOD_LEVEL);
        set.add(VanillaGuiLayers.VEHICLE_HEALTH);
        set.add(VanillaGuiLayers.AIR_LEVEL);
        set.add(VanillaGuiLayers.SELECTED_ITEM_NAME);
        set.add(VanillaGuiLayers.SPECTATOR_TOOLTIP);
        set.add(VanillaGuiLayers.EXPERIENCE_LEVEL);
    });

    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event)
    {
        Controllable.getControllerManager().completeSetup();
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onPlayerUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        //RumbleHandler.onPlayerUsingItem(event.getEntity(), event.getItem(), event.getDuration());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLayer(RenderGuiLayerEvent.Pre event)
    {
        if(OFFSET_LAYERS.contains(event.getName()))
        {
            PoseStack pose = event.getGuiGraphics().pose();
            pose.pushPose();
            pose.translate(0, -20, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLayer(RenderGuiLayerEvent.Post event)
    {
        if(OFFSET_LAYERS.contains(event.getName()))
        {
            event.getGuiGraphics().pose().popPose();
        }
    }
}
