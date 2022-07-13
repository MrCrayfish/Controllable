package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.screens.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.function.Supplier;

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

    private ControllerManager manager;

    public ScreenEvents(ControllerManager manager)
    {
        this.manager = manager;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onOpenGui(ScreenEvent.Init.Post event)
    {
        /* Resets the controller button states */
        ButtonBinding.resetButtonStates();

        if(event.getScreen() instanceof OptionsScreen)
        {
            int y = event.getScreen().height / 6 + 72 - 6;
            event.addListener(new ControllerButton((event.getScreen().width / 2) + 5 + 150 + 4, y, button -> Minecraft.getInstance().setScreen(new ControllerSelectionScreen(manager, event.getScreen()))));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlay(RenderGuiOverlayEvent.Pre event)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay().overlay()))
        {
            event.getPoseStack().translate(0, -20, 0);
            if(event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
            {
                RenderSystem.getModelViewStack().translate(0, -20, 0);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay().overlay()))
        {
            event.getPoseStack().translate(0, 20, 0);
            if(event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
            {
                RenderSystem.getModelViewStack().translate(0, 20, 0);
                RenderSystem.applyModelViewMatrix();
            }
        }
    }
}
