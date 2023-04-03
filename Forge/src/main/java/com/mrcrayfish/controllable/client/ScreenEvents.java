package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.screens.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ScreenEvents
{
    private static final List<IGuiOverlay> INCLUDED_OVERLAYS;

    static
    {
        ImmutableList.Builder<IGuiOverlay> builder = ImmutableList.builder();
        builder.add(VanillaGuiOverlay.HOTBAR.type().overlay());
        builder.add(VanillaGuiOverlay.PLAYER_HEALTH.type().overlay());
        builder.add(VanillaGuiOverlay.MOUNT_HEALTH.type().overlay());
        builder.add(VanillaGuiOverlay.ARMOR_LEVEL.type().overlay());
        builder.add(VanillaGuiOverlay.EXPERIENCE_BAR.type().overlay());
        builder.add(VanillaGuiOverlay.FOOD_LEVEL.type().overlay());
        builder.add(VanillaGuiOverlay.AIR_LEVEL.type().overlay());
        builder.add(VanillaGuiOverlay.CHAT_PANEL.type().overlay());
        builder.add(VanillaGuiOverlay.RECORD_OVERLAY.type().overlay());
        builder.add(VanillaGuiOverlay.ITEM_NAME.type().overlay());
        builder.add(VanillaGuiOverlay.JUMP_BAR.type().overlay());
        INCLUDED_OVERLAYS = builder.build();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlay(RenderGuiOverlayEvent.Pre event)
    {
        if(Config.CLIENT.client.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay().overlay()))
        {
            event.getPoseStack().translate(0, -20, 0);
            /*if(event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
            {
                //RenderSystem.getModelViewStack().translate(0, -20, 0);
            }*/
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event)
    {
        if(Config.CLIENT.client.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay().overlay()))
        {
            event.getPoseStack().translate(0, 20, 0);
            /*if(event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
            {
                //RenderSystem.getModelViewStack().translate(0, 20, 0);
                //RenderSystem.applyModelViewMatrix();
            }*/
        }
    }
}
