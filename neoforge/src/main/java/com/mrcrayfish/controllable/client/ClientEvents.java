package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    private static final int CONSOLE_HOTBAR_OFFSET = 25;

    private static final Set<ResourceLocation> OFFSET_LAYERS = Util.make(new HashSet<>(), set -> {
        set.add(VanillaGuiLayers.HOTBAR);
        set.add(VanillaGuiLayers.JUMP_METER);
        set.add(VanillaGuiLayers.EXPERIENCE_BAR);
        set.add(VanillaGuiLayers.SPECTATOR_TOOLTIP);
        set.add(VanillaGuiLayers.EXPERIENCE_LEVEL);
    });

    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event)
    {
        Controllable.getControllerManager().dispose();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void beforeRenderLayer(GuiGraphics graphics, GuiLayerManager.NamedLayer layer)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && OFFSET_LAYERS.contains(layer.name()))
        {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, -CONSOLE_HOTBAR_OFFSET, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLayer(RenderGuiLayerEvent.Post event)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && OFFSET_LAYERS.contains(event.getName()))
        {
            event.getGuiGraphics().pose().popPose();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Pre event)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            Minecraft.getInstance().gui.leftHeight += CONSOLE_HOTBAR_OFFSET;
            Minecraft.getInstance().gui.rightHeight += CONSOLE_HOTBAR_OFFSET;
        }
    }
}
